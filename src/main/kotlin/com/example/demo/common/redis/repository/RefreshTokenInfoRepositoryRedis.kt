package com.example.demo.common.redis.repository

import com.example.demo.common.login.jwt.JwtProperties
import com.example.demo.common.redis.dto.RefreshTokenInfoDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

//@Repository
//interface RefreshTokenInfoRepositoryRedis : CrudRepository<RefreshTokenInfoRedis, String>{
//    fun deleteByUserId(userId: String): RefreshTokenInfoRedis?
//}

@Repository
class RefreshTokenInfoRepositoryRedis(
    private val redisTemplate: RedisTemplate<String, RefreshTokenInfoDto>,
    private val jwtProperties: JwtProperties
) {
    companion object {
        private const val KEY_PREFIX = "refreshToken" // refreshToken:{userId}:{refreshToken}
    }

    init {
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
    }

    fun save(refreshTokenInfoDto: RefreshTokenInfoDto) {
        val key = "$KEY_PREFIX:${refreshTokenInfoDto.userId}:${refreshTokenInfoDto.refreshToken}"
        redisTemplate.opsForValue().set(key, refreshTokenInfoDto, jwtProperties.expire.refresh, TimeUnit.MILLISECONDS)
    }

    fun findByRefreshToken(refreshToken: String): RefreshTokenInfoDto? {
        val key = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken").firstOrNull()
        return key?.let { redisTemplate.opsForValue().get(it) }
    }

    fun findByUserId(userId: String): List<RefreshTokenInfoDto> {
        val keys = redisTemplate.keys("$KEY_PREFIX:$userId:*")
        val limitedKeys = keys.take(100)
        return limitedKeys.mapNotNull { key ->
            key?.let { redisTemplate.opsForValue().get(it) }
        }
    }

    fun deleteByRefreshToken(refreshToken: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:*:$refreshToken")
        redisTemplate.delete(keys)
    }

    fun deleteByUserId(userId: String) {
        val keys = redisTemplate.keys("$KEY_PREFIX:$userId:*")
        redisTemplate.delete(keys)
    }

    fun deleteBySecret(userId: String, secret: String): Boolean {
        val keys = redisTemplate.keys("$KEY_PREFIX:$userId:*")
        val refreshTokenInfoDto: RefreshTokenInfoDto = keys.mapNotNull { key ->
            key?.let { redisTemplate.opsForValue().get(it) }
        }.firstOrNull { x -> x.secret == secret } ?: return false

        val key = redisTemplate.keys("$KEY_PREFIX:${refreshTokenInfoDto.userId}:${refreshTokenInfoDto.refreshToken}")
        redisTemplate.delete(key)
        return true
    }
}
