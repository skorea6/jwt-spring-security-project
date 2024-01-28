package com.example.demo.common.config

import com.example.demo.common.login.filter.CustomUsernamePasswordAuthenticationFilter
import com.example.demo.common.login.handler.CustomLoginFailureHandler
import com.example.demo.common.login.handler.CustomLoginSuccessHandler
import com.example.demo.common.login.jwt.JwtAuthenticationFilter
import com.example.demo.common.login.jwt.JwtTokenProvider
import com.example.demo.common.login.service.CustomUserDetailsService
import com.example.demo.common.oauth2.handler.OAuth2LoginFailureHandler
import com.example.demo.common.oauth2.handler.OAuth2LoginSuccessHandler
import com.example.demo.common.oauth2.service.CustomOAuth2UserService
import com.example.demo.member.service.MemberService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val objectMapper: ObjectMapper,
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val entryPoint: AuthenticationEntryPoint,
    private val passwordEncoder: PasswordEncoder,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val customUserDetailsService: CustomUserDetailsService,
    private val customOAuth2UserService: CustomOAuth2UserService,
    @Value("\${frontend.url}") private val frontendUrl: String
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
//                    .requestMatchers(CorsUtils::isPreFlightRequest)
//                    .permitAll()
                    .requestMatchers("/api/member/find/**", "/api/member/signup/**", "/api/member/login/oauth2", "/api/member/login", "/api/member/token/refresh/issue")
                    .anonymous()
                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll() // cors 해결을 위해 preflight 모두 허용
                    .requestMatchers("/api/member/**").hasRole("MEMBER")
                    .anyRequest().permitAll()
            }
            .oauth2Login { oAuth2LoginConfigurer ->
                oAuth2LoginConfigurer
                    .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                    .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                    .userInfoEndpoint { it.userService(customOAuth2UserService) } // customUserService 설정
            }
            // 순서 : customUsernamePasswordAuthenticationFilter -> JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter
            .addFilterBefore(
                customUsernamePasswordAuthenticationFilter(), // 먼저 실행 (앞에 있는 필터가 통과하면 뒤에 있는 필터는 검사하지 않음)
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling { it.authenticationEntryPoint(entryPoint) }
            .build()
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val provider = DaoAuthenticationProvider()
        provider.setPasswordEncoder(passwordEncoder)
        provider.setUserDetailsService(customUserDetailsService)
        return ProviderManager(provider)
    }

    @Bean
    fun loginSuccessHandler(): CustomLoginSuccessHandler {
        return CustomLoginSuccessHandler(jwtTokenProvider, memberService, objectMapper)
    }

    @Bean
    fun loginFailureHandler(): CustomLoginFailureHandler {
        return CustomLoginFailureHandler(objectMapper)
    }

    @Bean
    fun customUsernamePasswordAuthenticationFilter(): CustomUsernamePasswordAuthenticationFilter {
        val customJsonUsernamePasswordLoginFilter = CustomUsernamePasswordAuthenticationFilter(objectMapper, memberService)
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager())
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler())
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler())
        customJsonUsernamePasswordLoginFilter.setFilterProcessesUrl("/api/member/login")
        customJsonUsernamePasswordLoginFilter.afterPropertiesSet()
        return customJsonUsernamePasswordLoginFilter
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(frontendUrl)
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "HEAD")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
