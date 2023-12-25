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
        private const val KEY_PREFIX = "refreshToken" // refreshToken:{userId}:{refreshToken}
    }

    fun save(userId: String, refreshToken: String) {
        val key = "$KEY_PREFIX:$userId:$refreshToken"
        redisTemplate.opsForValue().set(key, "", REFRESH_EXPIRATION_MILLISECONDS, TimeUnit.MILLISECONDS)
    }

    fun findByRefreshToken(refreshToken: String): String? {
        val key = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }

    fun deleteByRefreshToken(refreshToken: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken")
        redisTemplate.delete(keys)
    }

    fun deleteByUserId(userId: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:$userId:*")
        redisTemplate.delete(keys)
    }
}
