package com.example.demo.member.dto

import com.example.demo.common.annotation.ValidEnum
import com.example.demo.common.status.Gender
import com.example.demo.member.entity.Member
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private const val USER_ID_PATTERN = "^(?!kakao_|google_|naver_)[a-z0-9_]{4,20}$"
private const val PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\",./<>?|\\\\])[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\",./<>?|\\\\]{8,20}\$"
private const val NICK_PATTERN = "^(?!kakao_|google_|naver_)[a-zA-Z0-9가-힣!@#$%^&*()-+=\\[\\]{};':\",./<>?|\\\\ㄱ-ㅎㅏ-ㅣ_ ]{2,20}$"
private const val NAME_PATTERN = "^[a-zA-Z가-힣 ]{1,20}$"
private const val BIRTH_DATE_PATTERN = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$"

private const val USER_ID_MESSAGE = "영어 소문자, 숫자, 언더바만 가능하며, 4~20자리로 입력해주세요."
private const val PASSWORD_MESSAGE = "영어, 숫자, 특수문자를 포함한 8~20자리로 입력해주세요."
private const val NICK_MESSAGE = "영어, 한글, 숫자, 특정 특수문자만 가능하며, 2~20자리로 입력해주세요."
private const val NAME_MESSAGE = "영문, 한글만 가능하며, 1~20자리로 입력해주세요."
private const val BIRTH_DATE_MESSAGE = "날짜 형식(YYYY-MM-DD)을 확인해주세요."
private const val GENDER_MESSAGE = "MAN 이나 WOMAN 중 하나를 선택해주세요."

data class MemberDtoRequest(
    var id: Long?,

    @field:NotBlank
    @field:Pattern(regexp = USER_ID_PATTERN, message = USER_ID_MESSAGE)
    @JsonProperty("userId")
    private val _userId: String?,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @JsonProperty("password")
    private val _password: String?,

    @field:NotBlank
    @field:Pattern(regexp = NICK_PATTERN, message = NICK_MESSAGE)
    @JsonProperty("nick")
    private val _nick: String?,

    @field:Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE)
    @JsonProperty("name")
    private val _name: String?,

    @field:Pattern(regexp = BIRTH_DATE_PATTERN, message = BIRTH_DATE_MESSAGE)
    @JsonProperty("birthDate")
    private val _birthDate: String?,

    @field:ValidEnum(enumClass = Gender::class, message = GENDER_MESSAGE)
    @JsonProperty("gender")
    private val _gender: String?,

    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,
) {
    val userId: String
        get() = _userId!!
    val password: String
        get() = _password!!
    val nick: String
        get() = _nick!!
    val name: String?
        get() = _name
    val birthDate: LocalDate?
        get() = _birthDate?.toLocalDate()
    val gender: Gender?
        get() = _gender?.let { Gender.valueOf(it) }
    val email: String
        get() = _email!!

    private fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun toEntity(): Member =
        Member(
            id = id,
            userId = userId,
            password = password,
            nick = nick,
            name = name,
            birthDate = birthDate,
            gender = gender,
            email = email
        )
}

data class MemberUpdateDtoRequest(
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @JsonProperty("password")
    private val _password: String?,

    @field:NotBlank
    @field:Pattern(regexp = NICK_PATTERN, message = NICK_MESSAGE)
    @JsonProperty("nick")
    private val _nick: String?,

    @field:Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE)
    @JsonProperty("name")
    private val _name: String?,

    @field:Pattern(regexp = BIRTH_DATE_PATTERN, message = BIRTH_DATE_MESSAGE)
    @JsonProperty("birthDate")
    private val _birthDate: String?,

    @field:ValidEnum(enumClass = Gender::class, message = GENDER_MESSAGE)
    @JsonProperty("gender")
    private val _gender: String?,
) {
    val password: String?
        get() = _password
    val nick: String
        get() = _nick!!
    val name: String?
        get() = _name
    val birthDate: LocalDate?
        get() = _birthDate?.toLocalDate()
    val gender: Gender?
        get() = _gender?.let { Gender.valueOf(it) }

    private fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

data class MemberDtoForOauth2Request(
    @field:NotBlank
    @JsonProperty("token")
    private val _token: String?,

    @field:NotBlank
    @field:Pattern(regexp = NICK_PATTERN, message = NICK_MESSAGE)
    @JsonProperty("nick")
    private val _nick: String?,

    @field:Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE)
    @JsonProperty("name")
    private val _name: String?,

    @field:Pattern(regexp = BIRTH_DATE_PATTERN, message = BIRTH_DATE_MESSAGE)
    @JsonProperty("birthDate")
    private val _birthDate: String?,

    @field:ValidEnum(enumClass = Gender::class, message = GENDER_MESSAGE)
    @JsonProperty("gender")
    private val _gender: String?,
) {
    val token: String
        get() = _token!!
    val nick: String
        get() = _nick!!
    val name: String?
        get() = _name
    val birthDate: LocalDate?
        get() = _birthDate?.toLocalDate()
    val gender: Gender?
        get() = _gender?.let { Gender.valueOf(it) }

    private fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

/**
data class LoginDto(
    @field:NotBlank
    @JsonProperty("userId")
    private val _userId: String?,

    @field:NotBlank
    @JsonProperty("password")
    private val _password: String?,
) {
    val userId: String
        get() = _userId!!
    val password: String
        get() = _password!!
}
*/

data class TokenForOauth2Dto(
    @field:NotBlank
    @JsonProperty("token")
    private val _token: String?
) {
    val token: String
        get() = _token!!
}

data class TokenRefreshDto(
    @field:NotBlank
    @JsonProperty("refreshToken")
    private val _refreshToken: String?,
) {
    val refreshToken: String
        get() = _refreshToken!!
}

data class MemberDtoResponse(
    val id: Long = 0,
    val userId: String = "",
    val email: String = "",
    val nick: String = "",
    val name: String? = "",
    val birthDate: String? = "",
    val gender: String? = "",
    val imageUrl: String? = "",
    val socialType: String? = "",
    val socialId: String? = "",
    val socialNick: String? = "",
)
