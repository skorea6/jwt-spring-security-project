package com.example.demo.common.config

import com.example.demo.common.dto.CustomPrincipal
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*


@EnableJpaAuditing
@Configuration
class AuditConfig {
    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAware<String> {
            Optional.ofNullable(SecurityContextHolder.getContext())
                .map { it.authentication }
                .filter { it.isAuthenticated && !it.name.equals("anonymousUser") }
                .map { it.principal as CustomPrincipal }
                .map { it.username }
        }
    }
}
