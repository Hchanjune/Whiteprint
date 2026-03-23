package org.whiteprint.platform.infra.security.jwt.provider

import io.jsonwebtoken.Jwts
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.AccessTokenSigningKey
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenKey
import org.whiteprint.platform.core.security.provider.AccessTokenSigningKeyResolver
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.util.Date

class JwtTokenProvider: TokenProvider {
    override fun generateAccessToken(
        claims: AccessTokenClaims,
        key: AccessTokenSigningKey
    ): AccessToken {
        val jwt = Jwts.builder()
            .header()
            .keyId(key.keyId)
            .type("JWT")
            .and()
            .id(TsidGenerator.generate().toString())
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
            .id(TsidGenerator.generate().toString())
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