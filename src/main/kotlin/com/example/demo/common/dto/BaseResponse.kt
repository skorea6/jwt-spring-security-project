package com.example.demo.common.dto

import com.example.demo.common.status.ResultCode
import com.example.demo.util.DateUtil

data class BaseResponse<T>(
    val statusCode: Int = ResultCode.SUCCESS.statusCode,
    val statusMessage: String? = ResultCode.SUCCESS.message,
    val responseTime: String = DateUtil.getCurrentTime(),
    val data: T? = null
)