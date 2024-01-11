package com.example.demo.common.login.handler

import com.example.demo.common.dto.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class CustomLoginFailureHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        var errorMessage = "로그인 실패! 아이디 혹은 비밀번호를 확인해주세요."
//        val userId: Any? = request.getAttribute("userId")

        if (exception is AuthenticationServiceException) {
            if(exception.message != null){
                errorMessage = exception.message!!
            }
        }

        val finalResponse: String = objectMapper.writeValueAsString(BaseResponse(statusCode = HttpServletResponse.SC_BAD_REQUEST, statusMessage = errorMessage, data = null))
        response.status = HttpServletResponse.SC_BAD_REQUEST
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        response.writer.write(finalResponse)
    }
}
