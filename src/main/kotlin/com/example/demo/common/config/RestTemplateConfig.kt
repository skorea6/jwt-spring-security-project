package com.example.demo.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    // RestTemplate Bean 생성
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
