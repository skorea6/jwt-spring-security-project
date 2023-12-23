package com.example.demo.common.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUser(
    userId: String,
    password: String,
    authorities: Collection<GrantedAuthority>
) : User(userId, password, authorities)