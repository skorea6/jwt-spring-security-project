package com.example.demo.common.login.service

import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.member.entity.Member
import com.example.demo.member.repository.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        memberRepository.findByUserId(username)
            ?.let { return createUserDetails(it) } ?: throw UsernameNotFoundException("해당 유저는 없습니다.")

    private fun createUserDetails(member: Member): UserDetails =
        CustomPrincipal(
            member.userId,
            member.nick,
            member.email,
            member.password,
            member.memberRole!!.map { SimpleGrantedAuthority("ROLE_${it.role}") }
        )
}