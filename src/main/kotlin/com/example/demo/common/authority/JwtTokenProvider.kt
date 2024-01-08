package com.example.demo.common.authority

import com.example.demo.common.dto.CustomUser
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import kotlin.RuntimeException

const val ACCESS_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 10 // 60 * 60 (1시간)
const val REFRESH_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 20 // 60 * 60 * 24 * 30 (30일)

@Component
class JwtTokenProvider {
    @Value("\${jwt.access_secret}")
    lateinit var accessSecretKey: String

    @Value("\${jwt.refresh_secret}")
    lateinit var refreshSecretKey: String

    private val accessKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecretKey)) }
    private val refreshKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretKey)) }

    /**
     * Token 생성
     */
    fun createToken(authentication: Authentication): TokenInfo {
        val authorities: String = authentication
            .authorities
            .joinToString(",", transform = GrantedAuthority::getAuthority)

        val now = Date()
        val accessExpiration = Date(now.time + ACCESS_EXPIRATION_MILLISECONDS)
        val refreshExpiration = Date(now.time + REFRESH_EXPIRATION_MILLISECONDS)

        // Access Token
        val accessToken = Jwts
            .builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(accessExpiration)
            .signWith(accessKey, SignatureAlgorithm.HS256)
            .compact()

        // Refresh Token
        val refreshToken = Jwts
            .builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(refreshExpiration)
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(authentication.name, "Bearer", accessToken, refreshToken)
    }

    /**
     * Token 정보 추출
     */
    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getAccessTokenClaims(token)

        val auth = claims["auth"] ?: throw RuntimeException("잘못된 토큰입니다.")
//        val userId = claims["userId"] ?: throw RuntimeException("잘못된 토큰입니다.")

        // 권한 정보 추출
        val authorities: Collection<GrantedAuthority> = (auth as String)
            .split(",")
            .map { SimpleGrantedAuthority(it) }

        val principal = CustomUser(claims.subject, "", authorities)
//        val principal = PrincipalDetails(member = Member())
        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    /**
     * Token 검증
     */
    fun validateAccessToken(token: String): Boolean {
        try {
            getAccessTokenClaims(token)
            return true
        } catch (e: Exception) {
            print(e)
        }
        return false
    }

    fun isExpiredAccessToken(token: String): Boolean {
        try {
            val claims: Claims = getAccessTokenClaims(token)
            val now = Date()
            if(!claims.expiration.before(now)) {
                return false
            }
        } catch (e: Exception) {
            return true
        }
        return true
    }

    fun validateRefreshTokenAndCreateToken(refreshToken: String): TokenInfo {
        try {
            val refreshClaims: Claims = getRefreshTokenClaims(refreshToken)
            val now = Date()

            // accessToken 의 userId와 refreshToken 의 userId 가 동일한지 확인 (동일인 확인)
//            if(!accessClaims.subject.equals(refreshClaims.subject)){
//                throw ApiCustomException(HttpStatus.FORBIDDEN.value(), "비정상적인 Refresh 토큰 발급입니다.")
//            }

            // refresh 토큰의 만료시간이 지나지 않았을 경우, 새로운 access 토큰 발급
            val newAccessToken: String = Jwts
                .builder()
                .setSubject(refreshClaims.subject)
                .claim("auth", refreshClaims["auth"])
                .setIssuedAt(now)
                .setExpiration(Date(now.time + ACCESS_EXPIRATION_MILLISECONDS))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact()

            val newRefreshToken: String = Jwts
                .builder()
                .setSubject(refreshClaims.subject)
                .claim("auth", refreshClaims["auth"])
                .setIssuedAt(now)
                .setExpiration(Date(now.time + REFRESH_EXPIRATION_MILLISECONDS))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact()

            return TokenInfo(refreshClaims.subject, "Bearer", newAccessToken, newRefreshToken)
        } catch (e: Exception) {
            throw e
        }
    }

    fun validateAccessTokenForFilter(token: String): Boolean {
        try {
            getAccessTokenClaims(token)
            return true
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> {}  // Invalid JWT Token
                is MalformedJwtException -> {}  // Invalid JWT Token
                is ExpiredJwtException -> {}    // Expired JWT Token
                is UnsupportedJwtException -> {}    // Unsupported JWT Token
                is IllegalArgumentException -> {}   // JWT claims string is empty
                else -> {}  // else
            }
            throw e
        }
    }

    private fun getAccessTokenClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(accessKey)
            .build()
            .parseClaimsJws(token)
            .body

    private fun getRefreshTokenClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(refreshKey)
            .build()
            .parseClaimsJws(token)
            .body
}