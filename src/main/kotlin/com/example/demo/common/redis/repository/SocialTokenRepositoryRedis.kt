package com.example.demo.common.redis.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit


@Repository
class SocialTokenRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val KEY_PREFIX = "socialToken" // refreshToken:{socialToken}
    }

    init {
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
    }

    fun save(socialToken: String, userId: String) {
        val key = "$KEY_PREFIX:${socialToken}"
        redisTemplate.opsForValue().set(key, userId, 60 * 10, TimeUnit.SECONDS)
    }

    fun findBySocialToken(socialToken: String): String? {
        val key = redisTemplate.keys("$KEY_PREFIX:$socialToken").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }

    fun deleteBySocialToken(socialToken: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:$socialToken")
        redisTemplate.delete(keys)
    }
}
