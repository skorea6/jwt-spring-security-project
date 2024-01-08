package com.example.demo.member.entity

import com.example.demo.common.status.Gender
import com.example.demo.common.status.ROLE
import com.example.demo.member.dto.MemberDtoResponse
import jakarta.persistence.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(name = "uk_member_user_id", columnNames = ["userId"])]
)
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false, length = 30, updatable = false)
    val userId: String,

    @Column(nullable = false, length = 100)
    var password: String,

    @Column(nullable = false, length = 10)
    val name: String,

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    val birthDate: LocalDate,

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(nullable = false, length = 30)
    val email: String,
) {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    val memberRole: List<MemberRole>? = null

    private fun LocalDate.formatDate(): String =
        this.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    fun toDto(): MemberDtoResponse =
        MemberDtoResponse(id!!, userId, name, birthDate.formatDate(), gender.desc, email)
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
    @JoinColumn(foreignKey = ForeignKey(name = "fk_member_role_member_id"))
    val member: Member,

)