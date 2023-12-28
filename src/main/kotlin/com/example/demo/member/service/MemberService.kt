package com.example.demo.member.service

import com.example.demo.common.authority.JwtTokenProvider
import com.example.demo.common.authority.TokenInfo
import com.example.demo.common.exception.InvalidInputException
import com.example.demo.common.redis.dto.RefreshTokenInfoDto
import com.example.demo.common.redis.dto.RefreshTokenInfoDtoResponse
import com.example.demo.common.redis.repository.RefreshTokenInfoRepositoryRedis
import com.example.demo.common.status.ROLE
import com.example.demo.member.dto.LoginDto
import com.example.demo.member.dto.MemberDtoRequest
import com.example.demo.member.dto.MemberDtoResponse
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import com.example.demo.member.repository.MemberRepository
import com.example.demo.member.repository.MemberRoleRepository
import com.example.demo.util.BrowserInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenInfoRepositoryRedis: RefreshTokenInfoRepositoryRedis
) {
    /**
     * 회원가입
     */
    fun signUp(memberDtoRequest: MemberDtoRequest): String {
        // ID 중복 검사
        var member: Member? = memberRepository.findByUserId(memberDtoRequest.userId)
        if (member != null) {
            throw InvalidInputException("loginId", "이미 등록된 ID 입니다.")
        }

        member = memberDtoRequest.toEntity()
        member.password = passwordEncoder.encode(member.password)

        memberRepository.save(member)
        memberRoleRepository.save(MemberRole(null, ROLE.MEMBER, member))

        return "회원가입이 완료되었습니다."
    }

    /**
     * 로그인 -> 토큰 발행
     */
    fun login(request: HttpServletRequest, loginDto: LoginDto): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.userId, loginDto.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)
        val createToken: TokenInfo = jwtTokenProvider.createToken(authentication)

        val refreshTokenInfoDto: RefreshTokenInfoDto = createRefreshTokenInfoDto(
            request, loginDto.userId, createToken.refreshToken
        )
        refreshTokenInfoRepositoryRedis.save(refreshTokenInfoDto)

        return createToken
    }

    /**
     * 특정 Refresh 토큰 삭제
     */
    fun deleteRefreshToken(userId: String, secret: String): String {
        val deleteBySecret: Boolean = refreshTokenInfoRepositoryRedis.deleteBySecret(userId, secret)
        return if(deleteBySecret) "성공적으로 삭제되었습니다." else "찾을 수 없는 토큰입니다."
    }

    /**
     * 유저의 모든 Refresh 토큰 삭제
     */
    fun deleteAllRefreshToken(userId: String) {
        refreshTokenInfoRepositoryRedis.deleteByUserId(userId)
    }

    /**
     * Refresh 토큰 검증 후 토큰 재발급
     */
    fun validateRefreshTokenAndCreateToken(request: HttpServletRequest, refreshToken: String): TokenInfo{
        // Redis에서 refreshToken 존재 여부 확인
        refreshTokenInfoRepositoryRedis.findByRefreshToken(refreshToken)
            ?: throw InvalidInputException("refreshToken", "만료되거나 찾을 수 없는 Refresh 토큰입니다. 재로그인이 필요합니다.")

        // 새로운 accessToken , refreshToken
        val newTokenInfo: TokenInfo = jwtTokenProvider.validateRefreshTokenAndCreateToken(refreshToken)

        // 기존 refreshToken 제거 : refreshToken 은 1회용이어야
        refreshTokenInfoRepositoryRedis.deleteByRefreshToken(refreshToken)

        // 새로운 refreshToken Redis에 추가
        val refreshTokenInfoDto: RefreshTokenInfoDto = createRefreshTokenInfoDto(
            request, newTokenInfo.userId, newTokenInfo.refreshToken
        )
        refreshTokenInfoRepositoryRedis.save(refreshTokenInfoDto)

        return newTokenInfo
    }

    /**
     * 모든 Refresh 토큰 정보 가져오기
     * 실제로는 Refresh 토큰을 Response에 넘기지는 않음
     */
    fun getRefreshTokenList(userId: String): List<RefreshTokenInfoDtoResponse> {
        return refreshTokenInfoRepositoryRedis
            .findByUserId(userId)
            .map { x -> x.toResponse() }
            .sortedBy { x -> x.date }
            .reversed()
    }

    /**
     * 내 정보 조회
     */
    @Cacheable(key = "#userId", value = ["userInfo"])
    fun searchMyInfo(userId: String): MemberDtoResponse {
        val member: Member = memberRepository.findByUserId(userId) ?: throw InvalidInputException("userId", "회원 아이디(${userId})가 존재하지 않는 유저입니다.")
        return member.toDto()
    }

    /**
     * 내 정보 수정
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun saveMyInfo(userId: String, memberDtoRequest: MemberDtoRequest): String {
        val member: Member = memberRepository.findByUserId(userId) ?: throw InvalidInputException("userId", "회원 아이디(${userId})가 존재하지 않는 유저입니다.")
        memberDtoRequest.id = member.id

        val memberEntity: Member = memberDtoRequest.toEntity()
        memberEntity.password = passwordEncoder.encode(memberDtoRequest.password)

        memberRepository.save(memberEntity)
        return "수정이 완료되었습니다."
    }

    private fun createRefreshTokenInfoDto(
        request: HttpServletRequest,
        userId: String,
        refreshToken: String
    ): RefreshTokenInfoDto {
        val browserInfo: Map<String, String> = BrowserInfo().browserInfo(request)
        return RefreshTokenInfoDto(
            userId,
            refreshToken,
            browserInfo["header"]!!,
            browserInfo["browser"]!!,
            browserInfo["os"]!!,
            browserInfo["ipAddress"]!!
        )
    }
}