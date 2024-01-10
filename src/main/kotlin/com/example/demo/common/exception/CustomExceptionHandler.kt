package com.example.demo.common.exception

import com.example.demo.common.dto.BaseResponse
import com.example.demo.common.status.ResultCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException


@RestControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun methodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<BaseResponse<String>> {
//        val errors = mutableMapOf<String, String>()
//
//        ex.bindingResult.allErrors.forEach { error ->
//            val fieldName = (error as FieldError).field
//            val errorMessage = error.defaultMessage
//            errors[fieldName] = errorMessage ?: "Not Exception Message"
//        }
        val error = ex.bindingResult.allErrors[0]

        return ResponseEntity(BaseResponse(statusCode = ResultCode.BAD_REQUEST.statusCode, statusMessage = (error as FieldError).field + ": " + error.defaultMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidInputException::class)
    protected fun invalidInputException(ex: InvalidInputException): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity(BaseResponse(statusCode = ResultCode.BAD_REQUEST.statusCode, statusMessage = ex.fieldName + " : " + ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    protected fun illegalArgumentException(ex: IllegalArgumentException): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity(BaseResponse(statusCode = ResultCode.BAD_REQUEST.statusCode, statusMessage = ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): ResponseEntity<BaseResponse<String>> {
        val resultCode = when (ex) {
            is BadCredentialsException -> ResultCode.LOGIN_ERROR
            is BindException -> ResultCode.INVALID_DATA
            is HttpMessageNotReadableException -> ResultCode.INVALID_JSON
            is SignatureException, is SecurityException, is MalformedJwtException -> ResultCode.INVALID_ACCESS_TOKEN
            is ExpiredJwtException -> ResultCode.TOKEN_EXPIRED
            is NoHandlerFoundException -> ResultCode.NOT_FOUND
            else -> ResultCode.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity(BaseResponse(resultCode.statusCode, resultCode.message), HttpStatusCode.valueOf(resultCode.statusCode))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun httpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException, req: HttpServletRequest): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity(BaseResponse(statusCode = ResultCode.INTERNAL_SERVER_ERROR.statusCode, statusMessage = "Does not support request method '" + req.method + "'"), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // 기본 에러 처리
//    @ExceptionHandler(Exception::class)
//    protected fun defaultException(ex: Exception): ResponseEntity<BaseResponse<String>> {
////        val errors = mapOf("미처리 에러" to (ex.message ?: "Not Exception Message"))
//        return ResponseEntity(BaseResponse(statusCode = ResultCode.BAD_REQUEST.statusCode, statusMessage = ex.message), HttpStatus.BAD_REQUEST)
//    }

    // 커스텀 에러 처리
    @ExceptionHandler(ApiCustomException::class)
    protected fun apiCustomException(ex: ApiCustomException): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity(BaseResponse(statusCode = ex.statusCode, statusMessage = ex.statusMessage), HttpStatus.BAD_REQUEST)
    }
}
