package com.example.demo.member.controller

import com.example.demo.common.dto.BaseResponse
import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.login.TokenInfo
import com.example.demo.common.redis.dto.EmailVerificationDtoResponse
import com.example.demo.common.redis.dto.RefreshTokenDeleteDto
import com.example.demo.common.redis.dto.RefreshTokenInfoDtoResponse
import com.example.demo.member.dto.*
import com.example.demo.member.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/member")
@RestController
class MemberController(
    private val memberService: MemberService
) {
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    fun signUp(@RequestBody @Valid memberSignUpDtoRequest: MemberSignUpDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.signUp(memberSignUpDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 회원가입 - 이메일 인증번호 발송
     */
    @PostMapping("/signup/verification/email/send")
    fun signUpVerificationSendEmail(@RequestBody @Valid signUpVerificationSendEmailDtoRequest: SignUpVerificationSendEmailDtoRequest, request: HttpServletRequest): BaseResponse<EmailVerificationDtoResponse> {
        val emailVerificationDtoResponse: EmailVerificationDtoResponse = memberService.signUpVerificationSendEmail(request, signUpVerificationSendEmailDtoRequest)
        return BaseResponse(data = emailVerificationDtoResponse)
    }

    /**
     * 회원가입 - 이메일 인증번호 확인
     */
    @PostMapping("/signup/verification/email/check")
    fun signUpVerificationCheckEmail(@RequestBody @Valid verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.signUpVerificationCheckEmail(verificationCheckEmailDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 회원가입 Oauth2
     */
    @PostMapping("/signup/oauth2")
    fun signUpForOauth2(@RequestBody @Valid memberDtoForOauth2Request: MemberDtoForOauth2Request, request: HttpServletRequest): BaseResponse<TokenInfo> {
        val tokenInfo: TokenInfo = memberService.signUpForOauth2(request, memberDtoForOauth2Request)
        return BaseResponse(data = tokenInfo)
    }

    /**
     * OAuth2 회원가입 전 - 회원 정보 가져오기
     */
    @PostMapping("/signup/oauth2/info")
    fun memberInfoBeforeSignUpForOauth2(@RequestBody @Valid tokenForOauth2Dto: TokenForOauth2Dto): BaseResponse<MemberDtoResponse> {
        val memberDtoResponse: MemberDtoResponse = memberService.memberInfoBeforeSignUpForOauth2(tokenForOauth2Dto)
        return BaseResponse(data = memberDtoResponse)
    }

    /**
     * 로그인
     */
    /**
    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDto: LoginDto, request: HttpServletRequest): BaseResponse<TokenInfo> {
        val tokenInfo: TokenInfo = memberService.login(request, loginDto)
        return BaseResponse(data = tokenInfo)
    }
    */

    /**
     * 로그인 oAuth2
     */
    @PostMapping("/login/oauth2")
    fun loginForOauth2(@RequestBody @Valid tokenForOauth2Dto: TokenForOauth2Dto, request: HttpServletRequest): BaseResponse<TokenInfo> {
        val tokenInfo: TokenInfo = memberService.loginForOauth2(request, tokenForOauth2Dto)
        return BaseResponse(data = tokenInfo)
    }

    /**
     * 아이디 찾기 (이메일로 찾기)
     */
    @PostMapping("/find/user-id/by-email")
    fun findUserIdByEmail(@RequestBody @Valid findUserIdByEmailDto: FindUserIdByEmailDto, request: HttpServletRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.findUserIdByEmail(request, findUserIdByEmailDto)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 비밀번호 찾기 - 비밀번호 변경
     */
    @PostMapping("/find/password/by-email/reset")
    fun findPasswordByEmailResetPassword(@RequestBody @Valid findPasswordByEmailResetPasswordDtoRequest: FindPasswordByEmailResetPasswordDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.findPasswordByEmailResetPassword(findPasswordByEmailResetPasswordDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 비밀번호 찾기 - 이메일 인증번호 발송
     */
    @PostMapping("/find/password/by-email/email/send")
    fun findPasswordByEmailSendEmail(@RequestBody @Valid findPasswordByEmailSendEmailDtoRequest: FindPasswordByEmailSendEmailDtoRequest, request: HttpServletRequest): BaseResponse<EmailVerificationDtoResponse> {
        val emailVerificationDtoResponse: EmailVerificationDtoResponse = memberService.findPasswordByEmailSendEmail(request, findPasswordByEmailSendEmailDtoRequest)
        return BaseResponse(data = emailVerificationDtoResponse)
    }

    /**
     * 비밀번호 찾기 - 이메일 인증번호 확인
     */
    @PostMapping("/find/password/by-email/email/check")
    fun findPasswordByEmailCheckEmail(@RequestBody @Valid verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.findPasswordByEmailCheckEmail(verificationCheckEmailDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }


    /**
     * Refresh 토큰을 이용하여 토큰 재발급
     */
    @PostMapping("/token/refresh/issue")
    fun issueRefreshToken(@RequestBody @Valid tokenRefreshDto: TokenRefreshDto, request: HttpServletRequest): BaseResponse<TokenInfo> {
        val tokenInfo: TokenInfo = memberService.validateRefreshTokenAndCreateToken(request, tokenRefreshDto.refreshToken)
        return BaseResponse(data = tokenInfo)
    }

    /**
     * 모든 Refresh 토큰 정보 확인
     * 기기 정보 확인 API
     */
    @GetMapping("/token/refresh/list")
    fun getRefreshTokenList(): BaseResponse<List<RefreshTokenInfoDtoResponse>> {
        val response = memberService.getRefreshTokenList(getMemberUserId())
        return BaseResponse(data = response)
    }

    /**
     * 특정 Refresh 토큰 제거 API
     */
    @PostMapping("/token/refresh/delete")
    fun deleteRefreshToken(@RequestBody @Valid refreshTokenDeleteDto: RefreshTokenDeleteDto): BaseResponse<Unit> {
        val resultMsg: String = memberService.deleteRefreshToken(getMemberUserId(), refreshTokenDeleteDto.secret)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 로그아웃 API
     * 모든 Refresh 토큰 제거
     */
    @GetMapping("/token/refresh/logout")
    fun logoutRefreshToken(): BaseResponse<Unit> {
        memberService.deleteAllRefreshToken(getMemberUserId())
        return BaseResponse()
    }

    /**
     * 내 정보 보기
     */
    @GetMapping("/info")
    fun searchMyInfo(): BaseResponse<MemberDtoResponse> {
        val response = memberService.searchMyInfo(getMemberUserId())
        return BaseResponse(data = response)
    }

    /**
     * 내 정보 업데이트
     */
    @PostMapping("/update")
    fun updateMemberInfo(@RequestBody @Valid memberInfoUpdateDtoRequest: MemberInfoUpdateDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.updateMemberInfo(getMemberUserId(), memberInfoUpdateDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 비밀번호 업데이트
     */
    @PostMapping("/update/password")
    fun updateMemberPassword(@RequestBody @Valid memberPasswordUpdateDtoRequest: MemberPasswordUpdateDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.updateMemberPassword(getMemberUserId(), memberPasswordUpdateDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 이메일 업데이트 - 이메일 전송
     */
    @PostMapping("/update/email")
    fun updateMemberEmailSendEmail(@RequestBody @Valid memberEmailUpdateDtoRequest: MemberEmailUpdateDtoRequest, request: HttpServletRequest): BaseResponse<EmailVerificationDtoResponse> {
        val emailVerificationDtoResponse: EmailVerificationDtoResponse =
            memberService.updateMemberEmailSendEmail(request, getMemberUserId(), memberEmailUpdateDtoRequest)
        return BaseResponse(data = emailVerificationDtoResponse)
    }

    /**
     * 이메일 업데이트 - 이메일 확인
     */
    @PostMapping("/update/email/check")
    fun updateMemberEmailCheckEmail(@RequestBody @Valid verificationCheckEmailDtoRequest: VerificationCheckEmailDtoRequest, request: HttpServletRequest): BaseResponse<Unit> {
        val response: String = memberService.updateMemberEmailCheckEmail(request, getMemberUserId(), verificationCheckEmailDtoRequest)
        return BaseResponse(statusMessage = response)
    }


    // 유저 아이디 추출 - Spring Security
    private fun getMemberUserId(): String {
        return (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
    }
}