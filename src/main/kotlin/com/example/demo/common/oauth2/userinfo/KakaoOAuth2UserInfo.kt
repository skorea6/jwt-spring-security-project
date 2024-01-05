package com.example.demo.common.oauth2.userinfo;


class KakaoOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override val id: String
        get() = attributes["id"].toString()

    override val nickname: String
        get() {
            val account = attributes["kakao_account"] as Map<String, Any>? ?: return ""
            val profile = account["profile"] as Map<String, Any>? ?: return ""
            return profile["nickname"] as String
        }

    override val email: String
        get() {
            val account = attributes["kakao_account"] as Map<String, Any>? ?: return ""
            return account["email"] as String
        }

    override val imageUrl: String?
        get() {
            val account = attributes["kakao_account"] as Map<String, Any>? ?: return null
            val profile = account["profile"] as Map<String, Any>? ?: return null
            return profile["thumbnail_image_url"] as String?
        }
}
