package com.example.demo.common.login.handler

import com.example.demo.common.dto.BaseResponse
import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.login.TokenInfo
import com.example.demo.common.login.jwt.JwtTokenProvider
import com.example.demo.member.service.MemberService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class CustomLoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal: CustomPrincipal = authentication.principal as CustomPrincipal

        // JWT 토큰 생성 및 refreshToken 저장
        val createToken: TokenInfo = jwtTokenProvider.createToken(authentication)
        memberService.createRefreshTokenProcess(request, principal.username, createToken)

        // objectMapper로 Json 형식으로 변환 후, response
        val finalResponse: String = objectMapper.writeValueAsString(BaseResponse(data = createToken))
        response.status = HttpServletResponse.SC_OK
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        response.writer.write(finalResponse)
    }
}
