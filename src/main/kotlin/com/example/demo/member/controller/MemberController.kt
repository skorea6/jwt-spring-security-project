package com.example.demo.member.controller

import com.example.demo.common.authority.TokenInfo
import com.example.demo.common.dto.BaseResponse
import com.example.demo.common.dto.CustomUser
import com.example.demo.common.redis.dto.RefreshTokenDeleteDto
import com.example.demo.common.redis.dto.RefreshTokenInfoDtoResponse
import com.example.demo.member.dto.*
import com.example.demo.member.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
     * 로그인
     */
    @PostMapping("/login")
    fun login(@RequestBody @Valid loginDto: LoginDto, request: HttpServletRequest): BaseResponse<TokenInfo> {
        val tokenInfo: TokenInfo = memberService.login(request, loginDto)
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
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).username
        val response = memberService.getRefreshTokenList(userId)
        return BaseResponse(data = response)
    }

    /**
     * 특정 Refresh 토큰 제거 API
     */
    @PostMapping("/token/refresh/delete")
    fun deleteRefreshToken(@RequestBody @Valid refreshTokenDeleteDto: RefreshTokenDeleteDto): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).username
        val resultMsg: String = memberService.deleteRefreshToken(userId, refreshTokenDeleteDto.secret)
        return BaseResponse(statusMessage = resultMsg)
    }

    /**
     * 로그아웃 API
     * 모든 Refresh 토큰 제거
     */
    @GetMapping("/token/refresh/logout")
    fun logoutRefreshToken(): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).username
        memberService.deleteAllRefreshToken(userId)
        return BaseResponse()
    }

    /**
     * 내 정보 보기
     */
    @GetMapping("/info")
    fun searchMyInfo(): BaseResponse<MemberDtoResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).username
        val response = memberService.searchMyInfo(userId)
        return BaseResponse(data = response)
    }

    /**
     * 내 정보 저장
     */
    @PutMapping("/info")
    fun saveMyInfo(@RequestBody @Valid memberDtoRequest: MemberDtoRequest): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).username
        val resultMsg: String = memberService.saveMyInfo(userId, memberDtoRequest)
        return BaseResponse(statusMessage = resultMsg)
    }
}