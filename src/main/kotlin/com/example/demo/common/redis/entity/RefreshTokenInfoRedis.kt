package com.example.demo.common.redis.entity

import com.example.demo.common.authority.REFRESH_EXPIRATION_MILLISECONDS
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "refreshTokenInfo", timeToLive = REFRESH_EXPIRATION_MILLISECONDS/1000)
data class RefreshTokenInfoRedis (
    @Id
    val refreshToken : String,
    val userId : String,
)
