package com.example.demo.common.redis.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit


@Repository
class IpAddressAttemptRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, Int>
) {
    init {
        redisTemplate.keySerializer = StringRedisSerializer()
    }

    fun save(keyPrefix: String, ipAddress: String, count: Int) {
        val key = "$keyPrefix:${ipAddress}"
        redisTemplate.opsForValue().set(key, count, 60, TimeUnit.SECONDS)
    }

    fun findByIpAddress(keyPrefix: String, ipAddress: String): Int? {
        val key = redisTemplate.keys("$keyPrefix:${ipAddress}").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }
}
