package com.example.demo.common.login.jwt

import com.example.demo.common.exception.ApiCustomException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.cors.CorsUtils
import org.springframework.web.filter.GenericFilterBean

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        try {
            if (CorsUtils.isPreFlightRequest(request as HttpServletRequest)) {
                return
            }

            val token = resolveToken(request)

            if (jwtTokenProvider.validateAccessTokenForFilter(token)) {
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            request?.setAttribute("exception", e)
        }

        chain?.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String {
        val bearerToken = request.getHeader("Authorization")

        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            bearerToken.substring(7)
        } else {
            throw ApiCustomException(HttpStatus.UNAUTHORIZED.value(), "인증이 필요한 서비스입니다.")
        }
    }
}