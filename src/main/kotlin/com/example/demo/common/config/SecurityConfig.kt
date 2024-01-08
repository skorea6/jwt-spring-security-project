package com.example.demo.common.config

import com.example.demo.common.authority.JwtAuthenticationFilter
import com.example.demo.common.authority.JwtTokenProvider
import com.example.demo.common.oauth2.handler.OAuth2LoginFailureHandler
import com.example.demo.common.oauth2.handler.OAuth2LoginSuccessHandler
import com.example.demo.common.oauth2.service.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val entryPoint: AuthenticationEntryPoint,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val customOAuth2UserService: CustomOAuth2UserService
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/member/signup", "/api/member/signup/oauth2", "/api/member/signup/oauth2/info", "/api/member/login/oauth2", "/api/member/login", "/api/member/token/refresh/issue")
                    .anonymous()
                    .requestMatchers("/api/member/**").hasRole("MEMBER")
                    .anyRequest().permitAll()
            }
            .oauth2Login { oAuth2LoginConfigurer ->
                oAuth2LoginConfigurer
                    .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                    .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                    .userInfoEndpoint { it.userService(customOAuth2UserService) } // customUserService 설정
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider), // 먼저 실행 (앞에 있는 필터가 통과하면 뒤에 있는 필터는 검사하지 않음)
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling { it.authenticationEntryPoint(entryPoint) }
            .build()
    }
}