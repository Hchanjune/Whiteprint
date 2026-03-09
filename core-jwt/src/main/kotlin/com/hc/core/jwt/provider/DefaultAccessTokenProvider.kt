package com.hc.core.jwt.provider

import com.hc.core.jwt.model.AccessToken
import com.hc.core.jwt.model.AccessTokenKey
import com.hc.core.jwt.model.AccessTokenSubject
import com.hc.core.jwt.model.TokenSubjects
import com.hc.core.jwt.policy.AccessTokenPolicy
import io.jsonwebtoken.Jwts
import java.time.Instant
import java.util.Date

class DefaultAccessTokenProvider(
    private val accessTokenPolicy: AccessTokenPolicy,
    private val accessTokenKey: AccessTokenKey,
): AccessTokenProvider {

    override fun generateToken(accessTokenSubject: AccessTokenSubject): AccessToken {
        val now = Instant.now()
        val accessToken = Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .subject(TokenSubjects.ACCESS.name)
            .issuer(accessTokenPolicy.issuer)
            .audience().add(accessTokenPolicy.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTokenPolicy.ttl)))
            .claim("userId", accessTokenSubject.userId)
            .claim("authorities", accessTokenSubject.authorities)
            .signWith(accessTokenKey.value)
            .compact()
        return AccessToken(accessToken)
    }

}