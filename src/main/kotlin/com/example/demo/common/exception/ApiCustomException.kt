package com.example.demo.common.exception

data class ApiCustomException(
    val statusCode: Int,
    val statusMessage: String
) : RuntimeException()