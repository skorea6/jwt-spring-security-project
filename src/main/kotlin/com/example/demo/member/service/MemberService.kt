package com.example.demo.member.service

import com.example.demo.common.authority.JwtTokenProvider
import com.example.demo.common.authority.TokenInfo
import com.example.demo.common.exception.ApiCustomException
import com.example.demo.common.exception.InvalidInputException
import com.example.demo.common.redis.repository.RefreshTokenInfoRepositoryRedis
import com.example.demo.common.status.ROLE
import com.example.demo.member.dto.LoginDto
import com.example.demo.member.dto.MemberDtoRequest
import com.example.demo.member.dto.MemberDtoResponse
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import com.example.demo.member.repository.MemberRepository
import com.example.demo.member.repository.MemberRoleRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
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
    fun login(loginDto: LoginDto): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.userId, loginDto.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)
        val createToken: TokenInfo = jwtTokenProvider.createToken(authentication)

        refreshTokenInfoRepositoryRedis.save(createToken.refreshToken, loginDto.userId)
        return createToken
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
    fun validateRefreshTokenAndCreateToken(refreshToken: String): TokenInfo{
        // 새로운 accessToken , refreshToken
        val newTokenInfo: TokenInfo = jwtTokenProvider.validateRefreshTokenAndCreateToken(refreshToken)

        // Redis에서 refreshToken 존재 여부 확인
        refreshTokenInfoRepositoryRedis.findByRefreshToken(refreshToken)
            ?: throw InvalidInputException("refreshToken", "만료되거나 찾을 수 없는 Refresh 토큰입니다. 재로그인이 필요합니다.")

        // 기존 refreshToken 제거 : refreshToken 은 1회용이어야
        refreshTokenInfoRepositoryRedis.deleteByRefreshToken(refreshToken)

        // 새로운 refreshToken Redis에 추가
        refreshTokenInfoRepositoryRedis.save(newTokenInfo.refreshToken, newTokenInfo.userId)

        return newTokenInfo
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
}