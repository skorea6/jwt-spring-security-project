package com.example.demo.member.entity

import com.example.demo.common.domain.AuditingFields
import com.example.demo.common.status.Gender
import com.example.demo.common.status.ROLE
import com.example.demo.common.status.SocialType
import com.example.demo.common.status.UserType
import com.example.demo.member.dto.MemberDtoResponse
import jakarta.persistence.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_member_user_id", columnNames = ["userId"]),
        UniqueConstraint(name = "uk_member_email", columnNames = ["email"]),
        UniqueConstraint(name = "uk_member_nick", columnNames = ["nick"])
    ]
)
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false, length = 100, updatable = false)
    val userId: String,

    @Column(nullable = false, length = 100)
    var email: String,

    @Column(length = 100)
    var password: String,

    @Column(nullable = false, length = 100)
    var nick: String,

    @Column(length = 20)
    var name: String? = null,

    @Column(length = 1000)
    var imageUrl: String? = null, // 프로필 이미지

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    var userType: UserType? = null, // 일반회원 or 소셜회원 구분

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    var socialType: SocialType? = null, // KAKAO, NAVER, GOOGLE

    @Column(length = 100)
    var socialId: String? = null, // 로그인한 소셜 타입의 식별자 값 (일반 로그인인 경우 null)

    @Column(length = 100)
    var socialNick: String? = null, // 소셜 닉네임

    @Column
    var isSocialGuest: Boolean = false, // 소셜 로그인시 true

    @Column
    @Temporal(TemporalType.DATE)
    var birthDate: LocalDate? = null,

    @Column(length = 5)
    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

): AuditingFields() {
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "member", cascade = [CascadeType.ALL], targetEntity = MemberRole::class)
    var memberRole: List<MemberRole>? = mutableListOf()

    private fun LocalDate.formatDate(): String =
        this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun toDto(): MemberDtoResponse =
        MemberDtoResponse(id!!, userId, email, nick, name, birthDate?.formatDate(), gender?.name, imageUrl,
            userType?.name, socialType?.name, socialNick)

    fun existsMemberForSocial(socialType: SocialType?, socialId: String?, socialNick: String?, imageUrl: String?) {
        this.socialType = socialType
        this.socialId = socialId
        this.socialNick = socialNick
        this.imageUrl = imageUrl
        this.isSocialGuest = false
    }

    fun authorizeSocialMember(nick: String, name: String?, birthDate: LocalDate?, gender: Gender?) {
        this.nick = nick
        this.name = name
        this.birthDate = birthDate
        this.gender = gender
        this.isSocialGuest = false
    }
}

@Entity
class MemberRole(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    val role: ROLE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = ForeignKey(name = "fk_member_role_member_id"))
    val member: Member,
)