package com.example.demo.common.redis.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit


@Repository
class LoginAttemptRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, Int>
) {
    companion object {
        private const val KEY_PREFIX = "loginAttempt" // loginAttempt:{userId}:{ipAddress}
    }

    init {
        redisTemplate.keySerializer = StringRedisSerializer()
    }

    fun save(userId: String, ipAddress: String, count: Int) {
        val key = "$KEY_PREFIX:${userId}:${ipAddress}"
        redisTemplate.opsForValue().set(key, count, 60, TimeUnit.SECONDS)
    }

    fun findByUserIdAndIpAddress(userId: String, ipAddress: String): Int? {
        val key = redisTemplate.keys("$KEY_PREFIX:${userId}:${ipAddress}").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }
}
