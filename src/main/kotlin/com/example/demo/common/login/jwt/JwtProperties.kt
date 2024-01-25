package com.example.demo.common.login.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    val expire: Expire = Expire()
    var accessSecret: String = ""
    var refreshSecret: String = ""

    class Expire {
        var access: Long = 0
            get() = field * 60 * 1000 // 분을 밀리초로 변환
        var refresh: Long = 0
            get() = field * 60 * 1000
        var remainRefresh: Long = 0
            get() = field * 60 * 1000
    }
}