package com.example.demo.common.status

import org.springframework.http.HttpStatus

enum class Gender(val desc: String) {
    MAN("남"),
    WOMAN("여")
}

enum class ResultCode(val statusCode: Int, val message: String) {
    SUCCESS(HttpStatus.OK.value(), "성공"), // 200
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청하신 api를 찾을 수 없습니다."), // 404
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "항목이 올바르지 않습니다"), // 400
    INVALID_DATA(HttpStatus.BAD_REQUEST.value(), "데이터 처리 오류 발생"), // 400
    INVALID_JSON(HttpStatus.INTERNAL_SERVER_ERROR.value(), "전달된 JSON 형식이 올바르지 않습니다"), // 400
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 에러가 발생했습니다"), // 500
    TOKEN_EXPIRED(HttpStatus.FORBIDDEN.value(), "토큰이 만료되었습니다"), // 403
    LOGIN_ERROR(HttpStatus.BAD_REQUEST.value(), "아이디 혹은 비밀번호를 다시 확인하세요."), // 403
    INVALID_ACCESS_TOKEN(HttpStatus.FORBIDDEN.value(), "토큰이 유효하지 않습니다"), // 403
}

enum class ROLE {
    MEMBER
}

enum class SocialType {
    KAKAO,
    NAVER,
    GOOGLE
}