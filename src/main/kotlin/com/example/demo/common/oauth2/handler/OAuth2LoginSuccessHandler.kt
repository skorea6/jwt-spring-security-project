package com.example.demo.common.oauth2.handler;

import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.redis.repository.SocialTokenRepositoryRedis
import com.example.demo.util.RandomUtil
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException


@Component
@Transactional
class OAuth2LoginSuccessHandler(
    private val socialTokenRepositoryRedis: SocialTokenRepositoryRedis
) : AuthenticationSuccessHandler {


    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val oAuth2User = authentication.principal as CustomPrincipal

            val socialToken = RandomUtil().generateRandomString(64)
            socialTokenRepositoryRedis.save(socialToken, oAuth2User.username)

            // Member의 isSocialGuest가 true면 처음 요청한 회원이므로 임시 커스텀 토큰 발급 후 프론트의 소셜 회원가입 URL로 이동
            if (oAuth2User.isSocialGuest) {
                response.sendRedirect("http://localhost:3000/social/signup?token=$socialToken") // 프론트 소셜 회원가입 URL
            } else {
                response.sendRedirect("http://localhost:3000/social/login?token=$socialToken") // 프론트 소셜 로그인 URL
            }
        } catch (e: Exception) {
            throw e
        }
    }
}