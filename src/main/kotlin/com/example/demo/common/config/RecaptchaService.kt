package com.example.demo.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class RecaptchaService(
    private val restTemplate: RestTemplate,
    @Value("\${recaptcha.secret}") private val secretKey: String
) {
    fun verifyRecaptcha(response: String): Boolean {
        val verifyUrl = "https://www.google.com/recaptcha/api/siteverify"

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("secret", secretKey)
        params.add("response", response)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(params, headers)
        val responseEntity = restTemplate.postForEntity(verifyUrl, request, Map::class.java)
        val responseBody = responseEntity.body as Map<*, *>
        return responseBody["success"] as Boolean
    }

    fun verifyRecaptchaComplete(response: String) {
        require(verifyRecaptcha(response)){
            "구글 캡챠 검증에 실패하였습니다. 다시 시도해주세요."
        }
    }
}