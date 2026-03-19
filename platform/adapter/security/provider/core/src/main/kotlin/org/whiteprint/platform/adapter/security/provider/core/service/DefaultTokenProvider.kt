package org.whiteprint.platform.adapter.security.provider.core.service

import io.jsonwebtoken.Jwts
import org.whiteprint.platform.adapter.security.provider.core.model.AccessTokenSigningKey
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenClaims
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenKey
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenClaims
import java.util.Date
import kotlin.and

class DefaultTokenProvider: TokenProvider {
    override fun generateAccessToken(
        claims: AccessTokenClaims,
        key: AccessTokenSigningKey
    ): AccessToken {
        val jwt = Jwts.builder()
            .header()
            .keyId(key.keyId)
            .type("JWT")
            .and()
            .subject(claims.subject)
            .issuer(claims.issuer)
            .audience().add(claims.audience)
            .and()
            .issuedAt(Date.from(claims.issuedAt))
            .expiration(Date.from(claims.expiresAt))
            .claim("authorities", claims.authorities)
            .signWith(key.signingKey)
            .compact()
        return AccessToken(jwt)
    }

    override fun generateRefreshToken(
        claims: RefreshTokenClaims,
        key: RefreshTokenKey
    ): RefreshToken {
        val jwt = Jwts.builder()
            .header()
            .keyId(key.keyId)
            .type("JWT")
            .and()
            .subject(claims.subject)
            .issuer(claims.issuer)
            .audience().add(claims.audience)
            .and()
            .issuedAt(Date.from(claims.issuedAt))
            .expiration(Date.from(claims.expiresAt))
            .signWith(key.secretKey)
            .compact()
        return RefreshToken(jwt)
    }
    
}