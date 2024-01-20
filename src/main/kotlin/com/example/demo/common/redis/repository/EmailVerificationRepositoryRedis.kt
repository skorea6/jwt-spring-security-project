package com.example.demo.common.redis.repository

import com.example.demo.common.redis.dto.EmailVerificationDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit


@Repository
class EmailVerificationRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, EmailVerificationDto>
) {
    companion object {
        private const val KEY_PREFIX = "emailVerification" // emailVerification:{verificationToken}
    }

    init {
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
    }

    fun save(name: String, emailVerificationDto: EmailVerificationDto, timeout: Long) {
        val key = "$KEY_PREFIX$name:${emailVerificationDto.verificationToken}"
        redisTemplate.opsForValue().set(key, emailVerificationDto, timeout, TimeUnit.MINUTES)
    }

    fun findByVerificationToken(name: String, verificationToken: String): EmailVerificationDto? {
        val key = redisTemplate.keys("$KEY_PREFIX$name:$verificationToken").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }

    fun deleteByVerificationToken(name: String, verificationToken: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX$name:$verificationToken")
        redisTemplate.delete(keys)
    }
}
