package com.example.demo.common.redis.dto

import java.io.Serializable

data class EmailVerificationDto(
    val email: String = "",
    val verificationToken: String = "",
    val verificationNumber: String = "",
    var attemptCount: Int = 0,
    var isDone: Boolean = false
) : Serializable {
    fun toResponse(): EmailVerificationDtoResponse {
        return EmailVerificationDtoResponse(
            token = verificationToken
        )
    }
}

data class EmailVerificationDtoResponse(
    val token: String = ""
) : Serializable
