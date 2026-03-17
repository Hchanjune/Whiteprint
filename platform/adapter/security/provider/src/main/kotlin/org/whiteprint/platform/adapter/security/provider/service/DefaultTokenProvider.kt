package org.whiteprint.platform.adapter.security.provider.service

import com.hc.infra.security.provider.model.AccessTokenSigningKey
import com.hc.infra.security.provider.model.RefreshToken
import com.hc.infra.security.provider.model.RefreshTokenClaims
import com.hc.infra.security.provider.model.RefreshTokenKey
import org.whiteprint.platform.adapter.security.verifier.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.model.AccessTokenClaims
import io.jsonwebtoken.Jwts
import java.util.Date

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