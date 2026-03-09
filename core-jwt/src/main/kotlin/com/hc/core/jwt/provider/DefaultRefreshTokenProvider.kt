package com.hc.core.jwt.provider

import com.hc.core.jwt.model.RefreshToken
import com.hc.core.jwt.model.RefreshTokenKey
import com.hc.core.jwt.model.RefreshTokenSubject
import com.hc.core.jwt.model.TokenSubjects
import com.hc.core.jwt.policy.RefreshTokenPolicy
import io.jsonwebtoken.Jwts
import java.time.Instant
import java.util.Date

class DefaultRefreshTokenProvider(
    private val refreshTokenPolicy: RefreshTokenPolicy,
    private val refreshTokenKey: RefreshTokenKey,
): RefreshTokenProvider {

    override fun generateToken(refreshTokenSubject: RefreshTokenSubject): RefreshToken {
        val now = Instant.now()
        val refreshToken = Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .subject(TokenSubjects.REFRESH.name)
            .issuer(refreshTokenPolicy.issuer)
            .audience().add(refreshTokenPolicy.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTokenPolicy.ttl)))
            .claim("userId", refreshTokenSubject.userId)
            .signWith(refreshTokenKey.value)
            .compact()
        return RefreshToken(refreshToken)
    }


}