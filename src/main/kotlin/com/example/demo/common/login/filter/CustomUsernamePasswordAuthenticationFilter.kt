package com.example.demo.common.login.filter

import com.example.demo.member.service.MemberService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.StreamUtils
import java.io.IOException
import java.nio.charset.StandardCharsets


class CustomUsernamePasswordAuthenticationFilter(
    private val objectMapper: ObjectMapper,
    private val memberService: MemberService
) : AbstractAuthenticationProcessingFilter(DEFAULT_LOGIN_PATH_REQUEST_MATCHER) {

    @Throws(AuthenticationException::class, IOException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        if (request.contentType == null || request.contentType != CONTENT_TYPE) {
            throw AuthenticationServiceException("Authentication Content-Type not supported: " + request.contentType)
        }

        // RequestBody 가져오기 (String으로)
        val requestBody: String = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)

        if (requestBody.isBlank()) {
            throw AuthenticationServiceException("Empty or null request body")
        }

        // LoginDto로 변환 과정
        val loginDto: LoginDto = try {
            objectMapper.readValue(requestBody, LoginDto::class.java)
        } catch (e: Exception) {
            throw AuthenticationServiceException("Invalid JSON format", e)
        }

        // LoginDto의 userId, password 비어있는지 검사
        val userId: String? = loginDto.userId
        val password: String? = loginDto.password

        if (userId.isNullOrBlank() || password.isNullOrBlank()) {
            throw AuthenticationServiceException("userId와 password를 입력해주세요")
        }
//        request.setAttribute("userId", userId)

        val checkLoginAttempt: Boolean = memberService.checkLoginAttempt(request, userId) // 로그인 횟수 제한
        if(!checkLoginAttempt){
            throw AuthenticationServiceException("너무 많은 로그인 시도를 하였습니다. 잠시 후에 시도해주세요.")
        }

        val authRequest = UsernamePasswordAuthenticationToken(userId, password) //principal 과 credentials 전달
        return authenticationManager.authenticate(authRequest)
    }

    private data class LoginDto(
        val userId: String?,
        val password: String?
    )

    companion object {
        private const val DEFAULT_LOGIN_REQUEST_URL = "/api/member/login" // "/login"으로 오는 요청을 처리
        private const val HTTP_METHOD = "POST" // 로그인 HTTP 메소드는 POST
        private const val CONTENT_TYPE = "application/json" // JSON 타입의 데이터로 오는 로그인 요청만 처리
        private val DEFAULT_LOGIN_PATH_REQUEST_MATCHER = AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD) // "/login" + POST로 온 요청에 매칭된다.
    }
}
