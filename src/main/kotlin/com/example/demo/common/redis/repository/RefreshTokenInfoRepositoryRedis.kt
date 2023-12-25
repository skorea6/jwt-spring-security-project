package com.example.demo.common.redis.repository

import com.example.demo.common.authority.REFRESH_EXPIRATION_MILLISECONDS
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

//@Repository
//interface RefreshTokenInfoRepositoryRedis : CrudRepository<RefreshTokenInfoRedis, String>{
//    fun deleteByUserId(userId: String): RefreshTokenInfoRedis?
//}

@Repository
class RefreshTokenInfoRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val KEY_PREFIX = "refreshToken" // refreshToken:{refreshToken}:{userId}
    }

    fun save(userId: String, refreshToken: String) {
        val key = "$KEY_PREFIX:$userId:$refreshToken"
        redisTemplate.opsForValue().set(key, "", REFRESH_EXPIRATION_MILLISECONDS/1000, TimeUnit.SECONDS)
    }

    fun findByRefreshToken(refreshToken: String): String? {
        val key = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }

    fun deleteByRefreshToken(refreshToken: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken")
        keys.forEach { key ->
            redisTemplate.delete(key)
        }
    }

    fun deleteByUserId(userId: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:$userId:*")
        keys.forEach { key ->
            redisTemplate.delete(key)
        }
    }
}
