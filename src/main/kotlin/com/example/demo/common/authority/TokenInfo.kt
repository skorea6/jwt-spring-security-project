package com.example.demo.common.authority

data class TokenInfo(
    val userId: String,
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
)