package com.example.demo.common.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomPrincipal(
    private val userId: String,
    val nick: String,
    val email: String,
    private val password: String?,
    val isSocialGuest: Boolean,
    private val authorities: Collection<GrantedAuthority>,
    private val oAuth2Attributes: Map<String, Any>?
) : UserDetails, OAuth2User {
    // JWT Token Provider , CustomUserDetailsService
    constructor(
        userId: String,
        nick: String,
        email: String,
        password: String?,
        authorities: Collection<GrantedAuthority>,
    ) : this(userId, nick, email, password, false, authorities, null)

    // oAuth2
    constructor(
        userId: String,
        nick: String,
        email: String,
        isSocialGuest: Boolean,
        authorities: Collection<SimpleGrantedAuthority>,
        oAuth2Attributes: Map<String, Any>
    ) : this(userId, nick, email, null, isSocialGuest, authorities, oAuth2Attributes)


    override fun getName(): String {
        return userId
    }

    override fun getAttributes(): Map<String, Any>? {
        return oAuth2Attributes
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getUsername(): String {
        return userId
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
