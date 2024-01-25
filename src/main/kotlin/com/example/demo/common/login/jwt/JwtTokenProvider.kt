package com.example.demo.common.login.jwt

import com.example.demo.common.dto.CustomPrincipal
import com.example.demo.common.login.TokenInfo
import com.example.demo.member.entity.Member
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*

//const val ACCESS_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 10 // 60 * 30 (30분)
//const val REFRESH_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 20 // 60 * 60 * 24 * 30 * 3 (30일 * 3)
//const val REMAIN_REFRESH_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 5 // 60 * 60 * 24 * 30 (30일 * 1)

@Component
class JwtTokenProvider(
        jwtProperties: JwtProperties
) {
    private val accessSecretKey = jwtProperties.accessSecret
    private val refreshSecretKey = jwtProperties.refreshSecret

    private val accessExpireMilliseconds = jwtProperties.expire.access
    private val refreshExpireMilliseconds = jwtProperties.expire.refresh
    private val remainRefreshExpireMilliseconds = jwtProperties.expire.remainRefresh

//    @Value("\${jwt.access_secret}")
//    lateinit var accessSecretKey: String
//
//    @Value("\${jwt.refresh_secret}")
//    lateinit var refreshSecretKey: String

    private val accessKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecretKey)) }
    private val refreshKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretKey)) }

    /**
     * Token 생성 [일반 로그인]
     */
    fun createToken(authentication: Authentication): TokenInfo {
        val customPrincipal = authentication.principal as CustomPrincipal
        val authorities: String = customPrincipal
            .authorities
            .joinToString(",", transform = GrantedAuthority::getAuthority)

        return createTokenInfo(customPrincipal.name, customPrincipal.email, customPrincipal.nick, authorities)
    }

    /**
     * Token 생성 [Oauth2 전용]
     */
    fun createTokenForOauth2(member: Member): TokenInfo {
        val authorities: String = member.memberRole!!.joinToString(",") { "ROLE_${it.role}" }
        return createTokenInfo(member.userId, member.email, member.nick, authorities)
    }

    private fun createTokenInfo(userId: String, email: String, nick: String, authorities: String): TokenInfo {
        val now = Date()
        val accessExpiration = Date(now.time + accessExpireMilliseconds)
        val refreshExpiration = Date(now.time + refreshExpireMilliseconds)

        // Access Token
        val accessToken = Jwts
            .builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("nick", nick)
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(accessExpiration)
            .signWith(accessKey, SignatureAlgorithm.HS256)
            .compact()

        // Refresh Token
        val refreshToken = Jwts
            .builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("nick", nick)
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(refreshExpiration)
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(userId, "Bearer", accessToken, refreshToken)
    }

    /**
     * Token 정보 추출
     */
    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getAccessTokenClaims(token)

        val email = claims["email"] ?: throw RuntimeException("잘못된 토큰입니다.")
        val nick = claims["nick"] ?: throw RuntimeException("잘못된 토큰입니다.")
        val auth = claims["auth"] ?: throw RuntimeException("잘못된 토큰입니다.")

        // 권한 정보 추출
        val authorities: Collection<GrantedAuthority> = (auth as String)
            .split(",")
            .map { SimpleGrantedAuthority(it) }

        val principal = CustomPrincipal(claims.subject, nick as String, email as String, null, authorities)
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
                .claim("email", refreshClaims["email"])
                .claim("nick", refreshClaims["nick"])
                .claim("auth", refreshClaims["auth"])
                .setIssuedAt(now)
                .setExpiration(Date(now.time + accessExpireMilliseconds))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact()

            /**
             * refresh 토큰 만료시간 정책
             * - 남은 만료 기간 VALIDATE_REMAIN_EXPIRATION_MILLISECONDS 미만시에만 현재 날짜로부터 REFRESH_EXPIRATION_MILLISECONDS 연장
             * - 남은 만료 기간 VALIDATE_REMAIN_EXPIRATION_MILLISECONDS 이상일시에는 이전 만료 시간 그대로
             */
            var newRefreshExpiration: Long = now.time + refreshExpireMilliseconds
            val prevRefreshExpiration: Long = refreshClaims.expiration.time
            if(prevRefreshExpiration - now.time > remainRefreshExpireMilliseconds){ // 이전 만료 시간 그대로
                newRefreshExpiration = prevRefreshExpiration
            }

            val newRefreshToken: String = Jwts
                .builder()
                .setSubject(refreshClaims.subject)
                .claim("email", refreshClaims["email"])
                .claim("nick", refreshClaims["nick"])
                .claim("auth", refreshClaims["auth"])
                .setIssuedAt(now)
                .setExpiration(Date(newRefreshExpiration))
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