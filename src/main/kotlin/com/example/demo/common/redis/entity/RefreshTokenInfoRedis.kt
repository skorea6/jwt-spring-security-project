package com.example.demo.common.redis.entity

/**
 * 아래 @RedisHash 방법은 사용하지 않고, 순수 RedisTemplate을 사용하기로 결정함
 * 조금 더 커스텀하게 가져가고 불필요한 set들이 만들어지는 것을 방지하기 위함.
 */
/**
@RedisHash(value = "refreshTokenInfo", timeToLive = REFRESH_EXPIRATION_MILLISECONDS /1000)
data class RefreshTokenInfoRedis (
    @Id
    val refreshToken : String,
    val userId : String,
)
*/
