package com.example.demo.member.repository

import com.example.demo.common.status.SocialType
import com.example.demo.member.entity.Member
import com.example.demo.member.entity.MemberRole
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUserId(userId: String): Member?

    fun findByNick(nick: String): Member?
    fun findByEmail(email: String): Member?
    fun findByUserIdAndEmail(userId: String, email: String): Member?
    fun findByUserIdAndIsSocialGuest(userId: String, isSocialGuest: Boolean): Member?
    fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): Member?
    fun deleteByEmail(email: String)
}

interface MemberRoleRepository : JpaRepository<MemberRole, Long>