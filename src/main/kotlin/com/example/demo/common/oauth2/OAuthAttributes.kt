package com.example.demo.common.oauth2;

import com.example.demo.common.oauth2.userinfo.GoogleOAuth2UserInfo
import com.example.demo.common.oauth2.userinfo.KakaoOAuth2UserInfo
import com.example.demo.common.oauth2.userinfo.NaverOAuth2UserInfo
import com.example.demo.common.oauth2.userinfo.OAuth2UserInfo
import com.example.demo.common.status.SocialType
import com.example.demo.member.entity.Member
import com.example.demo.util.RandomUtil
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*


/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 데이터를 받는 데이터를 분기 처리하는 DTO 클래스
 */
class OAuthAttributes(// OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    val nameAttributeKey: String, // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)
    val oauth2UserInfo: OAuth2UserInfo
) {

    /**
     * of메소드로 OAuthAttributes 객체가 생성되어, 유저 정보들이 담긴 OAuth2UserInfo가 소셜 타입별로 주입된 상태
     * OAuth2UserInfo에서 socialId(식별값), nickname, imageUrl을 가져와서 build
     */
    fun toEntity(socialType: SocialType, oauth2UserInfo: OAuth2UserInfo, passwordEncoder: PasswordEncoder): Member {
        return Member(
            userId = socialType.name.lowercase(Locale.getDefault()) + "_" + RandomUtil().generateRandomString(12), // kakao_랜덤
            password = passwordEncoder.encode(RandomUtil().generateRandomString(16)), // 비밀번호 랜덤 16자
            email = oauth2UserInfo.email, // oauth2 이메일
            nick = oauth2UserInfo.nickname + "_" + RandomUtil().generateRandomString(6), // nick_랜덤
            imageUrl = oauth2UserInfo.imageUrl,
            socialType = socialType,
            socialId = oauth2UserInfo.id,
            socialNick = oauth2UserInfo.nickname,
            isSocialGuest = true // 최초 소셜로그인시 게스트로 설정 (회원가입 단계 추가: 추가적인 필드를 더 받기 위해서)
        )
    }

    companion object {
        /**
         * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
         * 파라미터 : userNameAttributeName -> OAuth2 로그인 시 키(PK)가 되는 값 / attributes : OAuth 서비스의 유저 정보들
         * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 제공하는
         * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
         */
        fun of(
            socialType: SocialType,
            userNameAttributeName: String, attributes: Map<String, Any>
        ): OAuthAttributes {
            if (socialType === SocialType.NAVER) {
                return ofNaver(userNameAttributeName, attributes)
            }
            return if (socialType === SocialType.KAKAO) {
                ofKakao(userNameAttributeName, attributes)
            } else ofGoogle(userNameAttributeName, attributes)
        }

        private fun ofKakao(
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuthAttributes {
            return OAuthAttributes(
                nameAttributeKey = userNameAttributeName,
                oauth2UserInfo = KakaoOAuth2UserInfo(attributes)
            )
        }

        private fun ofGoogle(
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuthAttributes {
            return OAuthAttributes(
                nameAttributeKey = userNameAttributeName,
                oauth2UserInfo = GoogleOAuth2UserInfo(attributes)
            )
        }

        private fun ofNaver(
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuthAttributes {
            return OAuthAttributes(
                nameAttributeKey = userNameAttributeName,
                oauth2UserInfo = NaverOAuth2UserInfo(attributes)
            )
        }
    }
}