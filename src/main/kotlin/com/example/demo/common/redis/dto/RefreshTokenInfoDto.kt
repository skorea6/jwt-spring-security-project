package com.example.demo.common.redis.dto

import com.example.demo.util.DateUtil
import com.example.demo.util.RandomUtil
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import java.io.Serializable

data class RefreshTokenInfoDto(
    val userId: String = "",
    val refreshToken: String = "",
    val header: String = "",
    val browser: String = "",
    val os: String = "",
    val ipAddress: String = "",
    val secret: String = RandomUtil().generateRandomString(15),
    val date: String = DateUtil().getCurrentTime(),
) : Serializable {
    fun toResponse(refreshToken: String): RefreshTokenInfoDtoResponse {
        return RefreshTokenInfoDtoResponse(
            userId = userId,
            header = header,
            browser = browser,
            os = os,
            ipAddress = ipAddress,
            current = this.refreshToken == refreshToken,
            secret = secret,
            date = date
        )
    }
}

data class RefreshTokenInfoDtoResponse(
    val userId: String,
    val header: String?,
    val browser: String?,
    val os: String?,
    val ipAddress: String?,
    val current: Boolean, // 현재 기기인지 확인
    val secret: String,
    val date: String
) : Serializable

data class RefreshTokenDeleteDto(
    @field:NotBlank
    @JsonProperty("secret")
    private val _secret: String?,
) {
    val secret: String
        get() = _secret!!
}

data class RefreshTokenDto(
        @field:NotBlank
        @JsonProperty("refreshToken")
        private val _refreshToken: String?,
) {
    val refreshToken: String
        get() = _refreshToken!!
}
