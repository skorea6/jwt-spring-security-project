package com.example.demo.member.dto

import com.example.demo.common.annotation.ValidEnum
import com.example.demo.common.status.Gender
import com.example.demo.common.status.UserType
import com.example.demo.member.entity.Member
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private const val USER_ID_PATTERN = "^(?!kakao_|google_|naver_)[a-z0-9_]{4,20}$"
private const val PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\",./<>?|\\\\`~])[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\",./<>?|\\\\`~]{8,20}\$"
private const val NICK_PATTERN = "^(?!kakao_|google_|naver_)[a-zA-Z0-9가-힣!@#$%^&*()-+=\\[\\]{};':\",./<>?|\\\\ㄱ-ㅎㅏ-ㅣ_ ]{2,20}$"
private const val NAME_PATTERN = "^[a-zA-Z가-힣 ]{1,20}$"
private const val BIRTH_DATE_PATTERN = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$"

private const val USER_ID_MESSAGE = "영어 소문자, 숫자, 언더바만 가능하며, 4~20자리로 입력해주세요."
private const val PASSWORD_MESSAGE = "영어, 숫자, 특정 특수문자를 포함한 8~20자리로 입력해주세요."
private const val NICK_MESSAGE = "영어, 한글, 숫자, 특정 특수문자만 가능하며, 2~20자리로 입력해주세요."
private const val NAME_MESSAGE = "영문, 한글만 가능하며, 1~20자리로 입력해주세요."
private const val BIRTH_DATE_MESSAGE = "날짜 형식(YYYY-MM-DD)을 확인해주세요."
private const val GENDER_MESSAGE = "MAN 이나 WOMAN 중 하나를 선택해주세요."

data class MemberSignUpDtoRequest(
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
    @JsonProperty("emailVerificationToken")
    private val _emailVerificationToken: String?,
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
    val emailVerificationToken: String
        get() = _emailVerificationToken!!

    private fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun toEntity(email: String): Member =
        Member(
            userId = userId,
            password = password,
            nick = nick,
            name = name,
            birthDate = birthDate,
            gender = gender,
            email = email,
            userType = UserType.EMAIL
        )
}

data class MemberInfoUpdateDtoRequest(
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

data class MemberPasswordUpdateDtoRequest(
    @field:NotBlank
    @JsonProperty("currentPassword")
    private val _currentPassword: String?,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @JsonProperty("password")
    private val _password: String?
) {
    val currentPassword: String
        get() = _currentPassword!!

    val password: String
        get() = _password!!
}

data class MemberEmailUpdateDtoRequest(
    @field:NotBlank
    @JsonProperty("currentPassword")
    private val _currentPassword: String?,

    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @JsonProperty("recaptchaResponse")
    private val _recaptchaResponse: String?
) {
    val currentPassword: String
        get() = _currentPassword!!

    val email: String
        get() = _email!!

    val recaptchaResponse: String
        get() = _recaptchaResponse!!
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

data class FindUserIdByEmailDto(
    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @JsonProperty("recaptchaResponse")
    private val _recaptchaResponse: String?
){
    val email: String
        get() = _email!!

    val recaptchaResponse: String
        get() = _recaptchaResponse!!
}

data class FindPasswordByEmailResetPasswordDtoRequest(
    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @JsonProperty("password")
    private val _password: String?,

    @field:NotBlank
    @JsonProperty("emailVerificationToken")
    private val _emailVerificationToken: String?,
){
    val password: String
        get() = _password!!

    val emailVerificationToken: String
        get() = _emailVerificationToken!!
}

data class FindPasswordByEmailSendEmailDtoRequest(
    @field:NotBlank
    @JsonProperty("userId")
    private val _userId: String?,

    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @JsonProperty("recaptchaResponse")
    private val _recaptchaResponse: String?
){
    val userId: String
        get() = _userId!!

    val email: String
        get() = _email!!

    val recaptchaResponse: String
        get() = _recaptchaResponse!!
}

data class MemberDeleteDtoRequest(
    @JsonProperty("currentPassword")
    private val _currentPassword: String?
) {
    val currentPassword: String?
        get() = _currentPassword
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
    val userType: String? = "",
    val socialType: String? = "",
    val socialNick: String? = "",
)

data class SignUpVerificationSendEmailDtoRequest(
    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @JsonProperty("recaptchaResponse")
    private val _recaptchaResponse: String?
){
    val email: String
        get() = _email!!

    val recaptchaResponse: String
        get() = _recaptchaResponse!!
}

data class VerificationCheckEmailDtoRequest(
    @field:NotBlank
    @JsonProperty("token")
    private val _token: String?,

    @field:NotBlank
    @JsonProperty("verificationNumber")
    private val _verificationNumber: String?
){
    val token: String
        get() = _token!!

    val verificationNumber: String
        get() = _verificationNumber!!
}
