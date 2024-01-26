package com.example.demo.common.oauth2.service;

import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.oauth2.OAuthAttributes
import com.example.demo.common.oauth2.userinfo.OAuth2UserInfo
import com.example.demo.common.status.ROLE
import com.example.demo.common.status.SocialType
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import com.example.demo.member.repository.MemberRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service


@Transactional
@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        /**
         * DefaultOAuth2UserService 객체를 생성하여, loadUser(userRequest)를 통해 DefaultOAuth2User 객체를 생성 후 반환
         * DefaultOAuth2UserService의 loadUser()는 소셜 로그인 API의 사용자 정보 제공 URI로 요청을 보내서
         * 사용자 정보를 얻은 후, 이를 통해 DefaultOAuth2User 객체를 생성 후 반환한다.
         * 결과적으로, OAuth2User는 OAuth 서비스에서 가져온 유저 정보를 담고 있는 유저
         */
        val delegate: OAuth2UserService<OAuth2UserRequest, OAuth2User> = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        /**
         * userRequest에서 registrationId 추출 후 registrationId으로 SocialType 저장
         * http://localhost:8080/oauth2/authorization/kakao에서 kakao가 registrationId
         * userNameAttributeName은 이후에 nameAttributeKey로 설정된다.
         */
        val registrationId = userRequest.clientRegistration.registrationId
        val socialType = getSocialType(registrationId)
        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName // OAuth2 로그인 시 키(PK)가 되는 값
        val attributes = oAuth2User.attributes // 소셜 로그인에서 API가 제공하는 userInfo의 Json 값(유저 정보들)

        // socialType에 따라 유저 정보를 통해 OAuthAttributes 객체 생성
        val extractAttributes = OAuthAttributes.of(socialType, userNameAttributeName, attributes)
        val createdMember: Member = getUser(extractAttributes, socialType) // getUser() 메소드로 User 객체 생성 후 반환

        // [보류] 이미 가입된 상태일 경우 : 멤버 이메일과 현재 소셜 이메일이 다를 경우 업데이트 필요.
//        if(createdMember.email != extractAttributes.oauth2UserInfo.email){
//            createdMember.email = extractAttributes.oauth2UserInfo.email
//            memberRepository.save(createdMember)
//        }

//        print(createdMember.userId)
//        print(createdMember.memberRole!!.joinToString(",") { "ROLE_${it.role}" })

        // DefaultOAuth2User를 구현한 CustomOAuth2User 객체를 생성해서 반환
        // nameAttributeKey : extractAttributes.nameAttributeKey
        return CustomPrincipal(
            userId = createdMember.userId,
            nick = createdMember.nick,
            email = createdMember.email,
            isSocialGuest = createdMember.isSocialGuest,
            authorities = createdMember.memberRole!!.map { SimpleGrantedAuthority("ROLE_${it.role}") },
            oAuth2Attributes = attributes
        )
    }

    private fun getSocialType(registrationId: String): SocialType {
        return if (NAVER == registrationId) {
            SocialType.NAVER
        }else if (KAKAO == registrationId) {
            SocialType.KAKAO
        } else{
            SocialType.GOOGLE
        }
    }

    /**
     * SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드
     * 만약 찾은 회원이 있다면, 그대로 반환하고 없다면 saveUser()를 호출하여 회원을 저장한다.
     */
    private fun getUser(attributes: OAuthAttributes, socialType: SocialType): Member {
        return memberRepository.findBySocialTypeAndSocialId(
            socialType,
            attributes.oauth2UserInfo.id
        ) ?: saveUser(attributes, socialType)
    }

    /**
     * OAuthAttributes의 toEntity() 메소드를 통해 빌더로 User 객체 생성 후 반환
     * 생성된 User 객체를 DB에 저장 : socialType, socialId, email, role 값만 있는 상태
     */
    private fun saveUser(attributes: OAuthAttributes, socialType: SocialType): Member {
        val oauth2UserInfo: OAuth2UserInfo = attributes.oauth2UserInfo

        // 이메일로 이미 가입된 회원이라면 그대로 해당 계정으로 로그인
        val findMemberWithEmail: Member? = memberRepository.findByEmail(oauth2UserInfo.email)
        if (findMemberWithEmail != null) {
            if(!findMemberWithEmail.isSocialGuest) {
                findMemberWithEmail.existsMemberForSocial(
                    socialType,
                    oauth2UserInfo.id,
                    oauth2UserInfo.nickname,
                    oauth2UserInfo.imageUrl
                )
                memberRepository.save(findMemberWithEmail)
                return findMemberWithEmail
            }else{
                // socialGuest가 true라면 해당 데이터 제거. 새로 만들기.
                memberRepository.deleteByEmail(oauth2UserInfo.email)
                memberRepository.flush()
            }
        }

        val createdMember: Member = attributes.toEntity(socialType, oauth2UserInfo, passwordEncoder)
        val memberRole = MemberRole(null, ROLE.MEMBER, createdMember)
        createdMember.memberRole = listOf(memberRole)

        memberRepository.save(createdMember)
        return createdMember
    }

    companion object {
        private const val NAVER = "naver"
        private const val KAKAO = "kakao"
    }
}