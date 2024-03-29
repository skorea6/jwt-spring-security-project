package com.example.demo.member.service

import com.example.demo.common.config.RecaptchaService
import com.example.demo.common.login.TokenInfo
import com.example.demo.common.login.jwt.JwtTokenProvider
import com.example.demo.common.redis.dto.EmailVerificationDto
import com.example.demo.common.redis.dto.EmailVerificationDtoResponse
import com.example.demo.common.redis.dto.RefreshTokenInfoDto
import com.example.demo.common.redis.dto.RefreshTokenInfoDtoResponse
import com.example.demo.common.redis.repository.*
import com.example.demo.common.status.ROLE
import com.example.demo.common.status.UserType
import com.example.demo.member.dto.*
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import com.example.demo.member.repository.DeletedMemberRepository
import com.example.demo.member.repository.MemberRepository
import com.example.demo.member.repository.MemberRoleRepository
import com.example.demo.util.BrowserInfo
import com.example.demo.util.MailUtil
import com.example.demo.util.RandomUtil
import com.example.demo.util.SenderDto
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
    private val deletedMemberRepository: DeletedMemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenInfoRepositoryRedis: RefreshTokenInfoRepositoryRedis,
    private val socialTokenRepositoryRedis: SocialTokenRepositoryRedis,
    private val loginAttemptRepositoryRedis: LoginAttemptRepositoryRedis,
    private val ipAddressAttemptRepositoryRedis: IpAddressAttemptRepositoryRedis,
    private val emailVerificationRepositoryRedis: EmailVerificationRepositoryRedis,
    private val recaptchaService: RecaptchaService,
    private val mailUtil: MailUtil
) {
    /**
     * 회원가입
     */
    fun signUp(memberSignUpDtoRequest: MemberSignUpDtoRequest): String {
        // 이메일 인증 시스템
        val emailVerificationDto: EmailVerificationDto = validateEmailVerification("SignUp", memberSignUpDtoRequest.emailVerificationToken)

        checkDuplicateUserId(memberSignUpDtoRequest.userId) // userId 중복 검사
        checkDuplicateNick(memberSignUpDtoRequest.nick) // nick 중복 검사

        val member: Member = memberSignUpDtoRequest.toEntity(emailVerificationDto.email)
        member.password = passwordEncoder.encode(member.password)

        memberRepository.save(member)
        memberRoleRepository.save(MemberRole(null, ROLE.MEMBER, member))

        completeEmailVerification("SignUp", memberSignUpDtoRequest.emailVerificationToken) // 이메일 인증 토큰 제거
        return "회원가입이 완료되었습니다."
    }

    /**
     * 회원가입 - 이메일 인증번호 발송
     */
    fun signUpVerificationSendEmail(signUpVerificationSendEmailDtoRequest: SignUpVerificationSendEmailDtoRequest): EmailVerificationDtoResponse {
        recaptchaService.verifyRecaptchaComplete(signUpVerificationSendEmailDtoRequest.recaptchaResponse)
        checkDuplicateEmail(signUpVerificationSendEmailDtoRequest.email) // email 중복 검사

        return verificationSendEmail(
            "SignUp",
            signUpVerificationSendEmailDtoRequest.email,
            "회원가입 인증번호 안내",
            "회원가입을 위해서 아래 인증코드를 입력해주세요."
        )
    }


    /**
     * 회원가입 - 이메일 인증번호 확인
     */
    fun signUpVerificationCheckEmail(verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): String {
        val emailVerificationDto: EmailVerificationDto = verificationCheckEmail("SignUp", verificationCheckEmailDtoRequest)
        emailVerificationRepositoryRedis.save("SignUp", emailVerificationDto, 15) // 10분 타임아웃 제한

        return "이메일 인증이 완료되었습니다."
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
     * 아이디 찾기 (이메일로 찾기)
     */
    fun findUserIdByEmail(request: HttpServletRequest, findUserIdByEmailDto: FindUserIdByEmailDto): String {
//        checkEmailVerificationAttempt(request) // 일정기간 동안 이메일 최대 전송 횟수 제한 (아이피 검사)
        recaptchaService.verifyRecaptchaComplete(findUserIdByEmailDto.recaptchaResponse)

        val member: Member = memberFindByEmail(findUserIdByEmailDto.email)

        exceptSocialMember(member, "already") // 소셜로 이미 가입된 계정이라는 메시지 출력

        mailUtil.send(
            SenderDto(
                to = arrayListOf(findUserIdByEmailDto.email),
                subject = "ABZ 사이트 - 아이디 찾기",
                content = "안녕하세요, ABZ 사이트입니다.<br><br>아이디 찾기를 요청해주셔서, 이메일로 가입된 아이디를 알려드립니다.<br>아이디는 <b>${member.userId}</b> 입니다."
            )
        )

        return "이메일이 전송 되었습니다! 이메일에서 아이디를 확인해주세요."
    }

    /**
     * 비밀번호 찾기 - 비밀번호 변경
     */
    fun findPasswordByEmailResetPassword(findPasswordByEmailResetPasswordDtoRequest: FindPasswordByEmailResetPasswordDtoRequest): String {
        // 이메일 인증 시스템
        val emailVerificationDto: EmailVerificationDto = validateEmailVerification("FindPassword", findPasswordByEmailResetPasswordDtoRequest.emailVerificationToken)
        val member: Member = memberFindByEmail(emailVerificationDto.email)

        member.password = passwordEncoder.encode(findPasswordByEmailResetPasswordDtoRequest.password)
        memberRepository.save(member)

        deleteAllRefreshToken(member.userId) // 모든 기기 로그아웃
        completeEmailVerification("FindPassword", findPasswordByEmailResetPasswordDtoRequest.emailVerificationToken)

        return "비밀번호 변경이 완료되었습니다."
    }

    /**
     * 비밀번호 찾기 - 이메일 인증번호 발송
     */
    fun findPasswordByEmailSendEmail(findPasswordByEmailSendEmailDtoRequest: FindPasswordByEmailSendEmailDtoRequest): EmailVerificationDtoResponse {
        recaptchaService.verifyRecaptchaComplete(findPasswordByEmailSendEmailDtoRequest.recaptchaResponse)

        val member: Member = memberRepository.findByUserIdAndEmail(
            findPasswordByEmailSendEmailDtoRequest.userId,
            findPasswordByEmailSendEmailDtoRequest.email
        ) ?: throw IllegalArgumentException("아이디와 이메일이 일치하는 회원이 없습니다.")

        exceptSocialMember(member, "already") // 소셜로 이미 가입된 계정이라는 메시지 출력

        return verificationSendEmail(
            "FindPassword",
            findPasswordByEmailSendEmailDtoRequest.email,
            "비밀번호 찾기를 위한 인증번호 안내",
            "${member.userId}님, 비밀번호 찾기를 위해서 아래 인증코드를 입력해주세요."
        )
    }

    /**
     * 비밀번호 찾기 - 이메일 인증번호 확인
     */
    fun findPasswordByEmailCheckEmail(verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): String {
        val emailVerificationDto: EmailVerificationDto = verificationCheckEmail("FindPassword", verificationCheckEmailDtoRequest)
        emailVerificationRepositoryRedis.save("FindPassword", emailVerificationDto, 15) // 10분 타임아웃 제한

        return "이메일 인증이 완료되었습니다."
    }


    /**
     * 특정 Refresh 토큰 삭제 - 리스트에서
     */
    fun deleteRefreshToken(userId: String, secret: String): String {
        val deleteBySecret: Boolean = refreshTokenInfoRepositoryRedis.deleteBySecret(userId, secret)
        require(deleteBySecret){
            "찾을 수 없는 토큰입니다."
        }
        return "성공적으로 삭제되었습니다."
    }

    /**
     * 특정 Refresh 토큰 삭제
     */
    fun deleteRefreshToken(refreshToken: String) {
        refreshTokenInfoRepositoryRedis.deleteByRefreshToken(refreshToken)
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
    fun refreshTokenList(userId: String, refreshToken: String): List<RefreshTokenInfoDtoResponse> {
        return refreshTokenInfoRepositoryRedis
            .findByUserId(userId)
            .map { x -> x.toResponse(refreshToken) }
            .sortedBy { x -> x.date }
            .reversed()
    }

    /**
     * 내 정보 조회
     */
    @Cacheable(key = "#userId", value = ["userInfo"])
    fun searchMyInfo(userId: String): MemberDtoResponse {
        return memberFindByUserId(userId).toDto()
    }

    /**
     * 내 정보 수정
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun updateMemberInfo(userId: String, memberInfoUpdateDtoRequest: MemberInfoUpdateDtoRequest): String {
        val member: Member = memberFindByUserId(userId)

        if(member.nick != memberInfoUpdateDtoRequest.nick){ // 현재 닉네임과 같을 경우 업데이트 X
            checkDuplicateNick(memberInfoUpdateDtoRequest.nick) // nick 중복 검사
            member.nick = memberInfoUpdateDtoRequest.nick
        }

        if(memberInfoUpdateDtoRequest.name !=  null){
            member.name = memberInfoUpdateDtoRequest.name
        }else{
            member.name = null
        }

        if(memberInfoUpdateDtoRequest.gender != null){
            member.gender = memberInfoUpdateDtoRequest.gender
        }else{
            member.gender = null
        }

        if(memberInfoUpdateDtoRequest.birthDate != null){
            member.birthDate = memberInfoUpdateDtoRequest.birthDate
        }else{
            member.birthDate = null
        }

        memberRepository.save(member)
        return "수정이 완료되었습니다."
    }


    /**
     * 비밀번호 수정
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun updateMemberPassword(userId: String, memberPasswordUpdateDtoRequest: MemberPasswordUpdateDtoRequest): String {
        val member: Member = memberFindByUserId(userId)
        exceptSocialMember(member, "basic")
        validatePassword(memberPasswordUpdateDtoRequest.currentPassword, member) // 현재 비밀번호가 맞는지 확인

        member.password = passwordEncoder.encode(memberPasswordUpdateDtoRequest.password)
        memberRepository.save(member)

        deleteAllRefreshToken(userId) // 모든 기기 로그아웃
        return "수정이 완료되었습니다."
    }


    /**
     * 이메일 수정 - 이메일 코드 전송
     */
    fun updateMemberEmailSendEmail(userId: String, memberEmailUpdateDtoRequest: MemberEmailUpdateDtoRequest): EmailVerificationDtoResponse {
        recaptchaService.verifyRecaptchaComplete(memberEmailUpdateDtoRequest.recaptchaResponse)

        val member: Member = memberFindByUserId(userId)
        exceptSocialMember(member, "basic")
        validatePassword(memberEmailUpdateDtoRequest.currentPassword, member) // 현재 비밀번호가 맞는지 확인

        require(member.email != memberEmailUpdateDtoRequest.email){
            "새로운 이메일 주소가 현재 이메일 주소와 동일합니다."
        }

        checkDuplicateEmail(memberEmailUpdateDtoRequest.email) // email 중복 검사

        return verificationSendEmail(
            "EmailUpdate",
            memberEmailUpdateDtoRequest.email,
            "이메일 업데이트 인증번호 안내",
             "${member.userId}님의 이메일 업데이트를 위해서 아래 인증코드를 입력해주세요."
        )
    }

    /**
     * 이메일 수정 - 이메일 코드 체크 후 수정
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun updateMemberEmailCheckEmail(request: HttpServletRequest, userId: String, verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): String {
        val member: Member = memberFindByUserId(userId)
        val emailVerificationDto: EmailVerificationDto = verificationCheckEmail("EmailUpdate", verificationCheckEmailDtoRequest)

        member.email = emailVerificationDto.email
        memberRepository.save(member)

        // 이메일 인증 토큰 제거
        emailVerificationRepositoryRedis.deleteByVerificationToken("EmailUpdate", emailVerificationDto.verificationToken)

        return "수정이 완료되었습니다."
    }

    /**
     * 회원탈퇴
     */
    @CacheEvict(key = "#userId", value = ["userInfo"])
    fun deleteMember(userId: String, memberDeleteDtoRequest: MemberDeleteDtoRequest): String {
        val member: Member = memberFindByUserId(userId)

        // 소셜 회원이 아닌 경우 비밀번호 검사
        if(member.userType != UserType.SOCIAL){
            if(memberDeleteDtoRequest.currentPassword.isNullOrEmpty()){
                throw IllegalArgumentException("현재 비밀번호를 입력해주세요")
            }
            validatePassword(memberDeleteDtoRequest.currentPassword!!, member)
        }

        deletedMemberRepository.save(member.toDeletedMember())
        memberRepository.deleteById(member.id!!)
        deleteAllRefreshToken(member.userId) // 모든 기기 로그아웃

        return "회원탈퇴가 완료되었습니다."
    }

    private fun memberFindByUserId(userId: String): Member {
        return memberRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("회원 아이디(${userId})가 존재하지 않는 유저입니다.")
    }

    private fun memberFindByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
            ?: throw IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다.")
    }

    // [소셜 로그인이 아닐 경우에만] 현재 비밀번호가 맞는지 확인하는 로직
    private fun validatePassword(
        password: String,
        member: Member
    ) {
        require(passwordEncoder.matches(password, member.password)) {
            "현재 비밀번호가 일치하지 않습니다."
        }
    }

    private fun exceptSocialMember(member: Member, messageType: String) {
        require(member.userType != UserType.SOCIAL) {
            when(messageType){
                "basic" -> "소셜 로그인 회원은 이용이 불가합니다."
                else -> {
                    "'${member.socialType!!.ko}' 소셜 회원으로 가입된 계정입니다."
                }
            }
        }
    }

    private fun checkDuplicateUserId(userId: String) {
        val findMember: Member? = memberRepository.findByUserId(userId)
        require(findMember == null){
            "이미 등록된 아이디입니다."
        }
    }

    private fun checkDuplicateNick(nick: String) {
        val findMember: Member? = memberRepository.findByNick(nick)
        require(findMember == null){
            "이미 등록된 닉네임입니다."
        }
    }

    private fun checkDuplicateEmail(email: String) {
        val findMember: Member? = memberRepository.findByEmail(email)
        if (findMember != null) {
            exceptSocialMember(findMember, "already") // 소셜로 이미 가입된 계정이라는 메시지 출력
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
        userId: String,
        limitedNum: Int
    ): Boolean {
        var countLoginAttempt = 1
        val ipAddress: String = BrowserInfo().getIpAddress(request)
        val loginAttempted: Int? = loginAttemptRepositoryRedis.findByUserIdAndIpAddress(userId, ipAddress)

        if (loginAttempted != null) {
            if (loginAttempted >= limitedNum) {
                return false
            }
            countLoginAttempt = loginAttempted + 1
        }
        loginAttemptRepositoryRedis.save(userId, ipAddress, countLoginAttempt)
        return true
    }

    fun checkIpAddressAttempt(
        request: HttpServletRequest,
        keyPrefix: String,
        limitedNum: Int
    ): Boolean {
        var countIpAddressAttempt = 1
        val ipAddress: String = BrowserInfo().getIpAddress(request)
        val ipAddressAttempted: Int? = ipAddressAttemptRepositoryRedis.findByIpAddress(keyPrefix, ipAddress)

        if (ipAddressAttempted != null) {
            if (ipAddressAttempted >= limitedNum) {
                return false
            }
            countIpAddressAttempt = ipAddressAttempted + 1
        }
        ipAddressAttemptRepositoryRedis.save(keyPrefix, ipAddress, countIpAddressAttempt)
        return true
    }

    /**
     * 임시로 아이피제한을 걸어놓는 로직을 짰지만, Captcha 도입을 위해 주석 처리함
     */
    /*
    private fun checkEmailVerificationAttempt(request: HttpServletRequest) {
        val checkAttempt: Boolean = checkIpAddressAttempt(request, "emailVerificationAttempt", 10) // 1분 최대 10번으로 제한
        require(checkAttempt) {
            "너무 많은 시도를 하였습니다. 잠시 후에 시도해주세요."
        }
    }
    */

    private fun verificationSendEmail(
        name: String,
        emailAddress: String,
        emailSubject: String,
        emailContent: String,
//        request: HttpServletRequest
    ): EmailVerificationDtoResponse {
//        checkEmailVerificationAttempt(request) // 일정기간 동안 이메일 최대 전송 횟수 제한 (아이피 검사)

        val verificationToken: String = RandomUtil().generateRandomString(32)
        val verificationNumber: String = RandomUtil().generateRandomNumber(6)

        val emailVerificationDto = EmailVerificationDto(
            email = emailAddress,
            verificationToken = verificationToken,
            verificationNumber = verificationNumber
        )
        emailVerificationRepositoryRedis.save(name, emailVerificationDto, 30) // 30분 타임아웃 제한

        mailUtil.send(
            SenderDto(
                to = arrayListOf(emailAddress),
                subject = "ABZ 사이트 - $emailSubject",
                content = "안녕하세요, ABZ 사이트입니다.<br><br>$emailContent<br>인증번호는 <b>$verificationNumber</b> 입니다."
            )
        )

        return emailVerificationDto.toResponse()
    }

    private fun verificationCheckEmail(name: String, verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): EmailVerificationDto {
        // 토큰을 이용하여 Redis에서 Dto 가져와서 인증번호와 동일한지 확인 후 isDone 업데이트
        val emailVerificationDto =
            emailVerificationRepositoryRedis.findByVerificationToken(
                name,
                verificationCheckEmailDtoRequest.token
            )
                ?: throw IllegalArgumentException("이메일 인증 시간이 초과되었습니다. 재시도해주세요.")

        require(!emailVerificationDto.isDone){
            "이미 이메일 인증이 완료된 상태입니다."
        }

        require(emailVerificationDto.attemptCount <= 10){
            "너무 많은 시도를 하였습니다. 처음부터 재시도해주세요."
        }

        if (emailVerificationDto.verificationNumber != verificationCheckEmailDtoRequest.verificationNumber) {
            emailVerificationDto.attemptCount++
            emailVerificationRepositoryRedis.save(name, emailVerificationDto, 30)
            throw IllegalArgumentException("인증번호가 올바르지 않습니다.")
        }

        emailVerificationDto.isDone = true
        return emailVerificationDto
    }

    // 이메일 토큰 확인하기
    private fun validateEmailVerification(name: String, emailVerificationToken: String): EmailVerificationDto {
        val emailVerificationDto: EmailVerificationDto =
            emailVerificationRepositoryRedis.findByVerificationToken(
                name,
                emailVerificationToken
            )
                ?: throw IllegalArgumentException("시간이 초과되었습니다. 재시도해주세요.")

        require(emailVerificationDto.isDone) {
            "이메일 인증이 완료되지 않았습니다."
        }
        return emailVerificationDto
    }

    // 이메일 인증을 모두 성공한 이후
    private fun completeEmailVerification(name: String, emailVerificationToken: String) {
        emailVerificationRepositoryRedis.deleteByVerificationToken(
            name,
            emailVerificationToken
        )
    }
}
