package com.example.demo.member.controller

import com.example.demo.common.dto.BaseResponse
import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.login.TokenInfo
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
    fun signUp(@RequestBody @Valid memberDtoRequest: MemberDtoRequest): BaseResponse<Unit> {
        val resultMsg: String = memberService.signUp(memberDtoRequest)
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
    @GetMapping("/signup/oauth2/info")
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
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
        val response = memberService.getRefreshTokenList(userId)
        return BaseResponse(data = response)
    }

    /**
     * 특정 Refresh 토큰 제거 API
     */
    @PostMapping("/token/refresh/delete")
    fun deleteRefreshToken(@RequestBody @Valid refreshTokenDeleteDto: RefreshTokenDeleteDto): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
        val resultMsg: String = memberService.deleteRefreshToken(userId, refreshTokenDeleteDto.secret)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 로그아웃 API
     * 모든 Refresh 토큰 제거
     */
    @GetMapping("/token/refresh/logout")
    fun logoutRefreshToken(): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
        memberService.deleteAllRefreshToken(userId)
        return BaseResponse()
    }

    /**
     * 내 정보 보기
     */
    @GetMapping("/info")
    fun searchMyInfo(): BaseResponse<MemberDtoResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
        val response = memberService.searchMyInfo(userId)
        return BaseResponse(data = response)
    }

    /**
     * 내 정보 저장
     */
    @PutMapping("/info")
    fun updateMyInfo(@RequestBody @Valid memberUpdateDtoRequest: MemberUpdateDtoRequest): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomPrincipal).username
        val resultMsg: String = memberService.updateMyInfo(userId, memberUpdateDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }
}