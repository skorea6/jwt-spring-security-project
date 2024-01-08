package com.example.demo.member.service

import com.example.demo.common.login.TokenInfo
import com.example.demo.common.login.jwt.JwtTokenProvider
import com.example.demo.common.redis.dto.RefreshTokenInfoDto
import com.example.demo.common.redis.dto.RefreshTokenInfoDtoResponse
import com.example.demo.common.redis.repository.LoginAttemptRepositoryRedis
import com.example.demo.common.redis.repository.RefreshTokenInfoRepositoryRedis
import com.example.demo.common.redis.repository.SocialTokenRepositoryRedis
import com.example.demo.common.status.ROLE
import com.example.demo.member.dto.*
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import com.example.demo.member.repository.MemberRepository
import com.example.demo.member.repository.MemberRoleRepository
import com.example.demo.util.BrowserInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenInfoRepositoryRedis: RefreshTokenInfoRepositoryRedis,
    private val socialTokenRepositoryRedis: SocialTokenRepositoryRedis,
    private val loginAttemptRepositoryRedis: LoginAttemptRepositoryRedis
) {
    /**
     * 회원가입
     */
    fun signUp(memberDtoRequest: MemberDtoRequest): String {
        checkDuplicateUserId(memberDtoRequest.userId) // userId 중복 검사
        checkDuplicateNick(memberDtoRequest.nick) // nick 중복 검사
        checkDuplicateEmail(memberDtoRequest.email) // email 중복 검사

        // TODO: 이메일 인증 시스템 구축
        val member: Member = memberDtoRequest.toEntity()
        member.password = passwordEncoder.encode(member.password)

        memberRepository.save(member)
        memberRoleRepository.save(MemberRole(null, ROLE.MEMBER, member))

        return "회원가입이 완료되었습니다."
    }

    /**
     * OAuth2 회원가입
     */
    fun signUpForOauth2(request: HttpServletRequest, memberDtoForOauth2Request: MemberDtoForOauth2Request): TokenInfo {
        val member: Member = checkSocialTokenAndReturnMemberForSignUp(memberDtoForOauth2Request.token)
        checkDuplicateNick(memberDtoForOauth2Request.nick) // nick 중복 검사

        member.authorizeSocialMember(
            memberDtoForOauth2Request.nick,
            memberDtoForOauth2Request.name,
            memberDtoForOauth2Request.birthDate,
            memberDtoForOauth2Request.gender
        ) // 회원가입이 완료되면 isSocialGuest를 false로 변경

        memberRepository.save(member) // update

        socialTokenRepositoryRedis.deleteBySocialToken(memberDtoForOauth2Request.token)

        return loginForOauth2Process(member, request) // login Process
    }

    /**
     * OAuth2 회원가입 전 - 회원 정보 가져오기
     */
    fun memberInfoBeforeSignUpForOauth2(tokenForOauth2Dto: TokenForOauth2Dto): MemberDtoResponse {
        val member: Member = checkSocialTokenAndReturnMemberForSignUp(tokenForOauth2Dto.token)
        return member.toDto()
    }

    /**
     * 로그인 -> 토큰 발행 (CustomUsernamePasswordAuthenticationFilter : CustomLoginSuccessHandler, CustomFailureSuccessHandler 로 이동)
     */
    /**
    fun login(request: HttpServletRequest, loginDto: LoginDto): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.userId, loginDto.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        val createToken: TokenInfo = jwtTokenProvider.createToken(authentication)
        createRefreshTokenProcess(request, loginDto.userId, createToken)

        return createToken
    }
    */

    /**
     * Oauth2 로그인 -> 토큰 발행
     */
    fun loginForOauth2(request: HttpServletRequest, tokenForOauth2Dto: TokenForOauth2Dto): TokenInfo {
        val member: Member = checkSocialTokenAndReturnMemberForLogin(tokenForOauth2Dto.token)
//        checkLoginAttempt(request, member.userId) // 로그인 시도 횟수 제한

        socialTokenRepositoryRedis.deleteBySocialToken(tokenForOauth2Dto.token)

        return loginForOauth2Process(member, request)
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
    fun validateRefreshTokenAndCreateToken(request: HttpServletRequest, refreshToken: String): TokenInfo {
        // Redis에서 refreshToken 존재 여부 확인
        refreshTokenInfoRepositoryRedis.findByRefreshToken(refreshToken)
            ?: throw IllegalArgumentException("만료되거나 찾을 수 없는 Refresh 토큰입니다. 재로그인이 필요합니다.")

        // 새로운 accessToken , refreshToken
        val newTokenInfo: TokenInfo = jwtTokenProvider.validateRefreshTokenAndCreateToken(refreshToken)

        // 기존 refreshToken 제거 : refreshToken 은 1회용이어야
        refreshTokenInfoRepositoryRedis.deleteByRefreshToken(refreshToken)

        // 새로운 refreshToken Redis에 추가
        createRefreshTokenProcess(request, newTokenInfo.userId, newTokenInfo)

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
        val member: Member = memberRepository.findByUserId(userId) ?: throw IllegalArgumentException("회원 아이디(${userId})가 존재하지 않는 유저입니다.")
        return member.toDto()
    }

    /**
     * 내 정보 수정
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun updateMyInfo(userId: String, memberUpdateDtoRequest: MemberUpdateDtoRequest): String {
        val member: Member = memberRepository.findByUserId(userId) ?: throw IllegalArgumentException("회원 아이디(${userId})가 존재하지 않는 유저입니다.")

        // 비번 변경시 : 백단에서 모든 기기에서 로그아웃, 프론트단에서 accessToken 쿠키 삭제 후 재로그인 과정 필요.
        if(memberUpdateDtoRequest.password != null){
            member.password = passwordEncoder.encode(memberUpdateDtoRequest.password)
            deleteAllRefreshToken(userId)
        }

        if(member.nick != memberUpdateDtoRequest.nick){ // 현재 닉네임과 같을 경우 업데이트 X
            checkDuplicateNick(memberUpdateDtoRequest.nick) // nick 중복 검사
            member.nick = memberUpdateDtoRequest.nick
        }

        if(memberUpdateDtoRequest.name != null){
            member.name = memberUpdateDtoRequest.name
        }

        if(memberUpdateDtoRequest.gender != null){
            member.gender = memberUpdateDtoRequest.gender
        }

        if(memberUpdateDtoRequest.birthDate != null){
            member.birthDate = memberUpdateDtoRequest.birthDate
        }

        memberRepository.save(member)
        return "수정이 완료되었습니다."
    }

    private fun checkDuplicateUserId(userId: String) {
        val findMember: Member? = memberRepository.findByUserId(userId)
        if (findMember != null) {
            throw IllegalArgumentException("이미 등록된 ID 입니다.")
        }
    }

    private fun checkDuplicateNick(nick: String) {
        val findMember: Member? = memberRepository.findByNick(nick)
        if (findMember != null) {
            throw IllegalArgumentException("이미 등록된 닉네임입니다.")
        }
    }

    private fun checkDuplicateEmail(email: String) {
        val findMember: Member? = memberRepository.findByEmail(email)
        if (findMember != null) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
    }

    private fun checkSocialTokenAndReturnMemberForSignUp(token: String): Member {
        val userId: String = socialTokenRepositoryRedis.findBySocialToken(token)
            ?: throw IllegalArgumentException("만료 시간이 지났습니다. 다시 시도해주세요.")

        return memberRepository.findByUserIdAndIsSocialGuest(userId, true) ?: throw IllegalArgumentException("존재하지 않는 회원이거나 이미 가입된 회원입니다.")
    }

    private fun checkSocialTokenAndReturnMemberForLogin(token: String): Member {
        val userId: String = socialTokenRepositoryRedis.findBySocialToken(token)
            ?: throw IllegalArgumentException("만료 시간이 지났습니다. 다시 시도해주세요.")

        return memberRepository.findByUserIdAndIsSocialGuest(userId, false) ?: throw IllegalArgumentException("존재하지 않는 회원입니다.")
    }

    private fun loginForOauth2Process(
        member: Member,
        request: HttpServletRequest
    ): TokenInfo {
        val createToken: TokenInfo = jwtTokenProvider.createTokenForOauth2(member)
        createRefreshTokenProcess(request, member.userId, createToken)

        return createToken
    }

    fun createRefreshTokenProcess(
        request: HttpServletRequest,
        userId: String,
        createToken: TokenInfo
    ) {
        val refreshTokenInfoDto: RefreshTokenInfoDto = createRefreshTokenInfoDto(
            request, userId, createToken.refreshToken
        )
        refreshTokenInfoRepositoryRedis.save(refreshTokenInfoDto)
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

    // 1분 안에 20회 이상 로그인 시도(성공, 실패) 막기 : 아이디와 IP 주소로 판단
    fun checkLoginAttempt(
        request: HttpServletRequest,
        userId: String
    ): Boolean {
        var countLoginAttempt = 1
        val ipAddress: String = BrowserInfo().getIpAddress(request)
        val loginAttempted: Int? = loginAttemptRepositoryRedis.findByUserIdAndIpAddress(userId, ipAddress)

        if (loginAttempted != null) {
            if (loginAttempted >= 15) {
                return false
            }
            countLoginAttempt = loginAttempted + 1
        }
        loginAttemptRepositoryRedis.save(userId, ipAddress, countLoginAttempt)
        return true
    }
}
