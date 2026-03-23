package org.whiteprint.platform.infra.security.jwt.provider

import io.jsonwebtoken.Jwts
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigningKeyResolver
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.time.Instant
import java.util.Date

class JwtTokenProvider(
    private val policy: TokenPolicy,
    private val accessTokenSigningKeyResolver: AccessTokenSigningKeyResolver,
    private val refreshTokenKeyResolver: RefreshTokenKeyResolver,
): TokenProvider {
    override fun generateAccessToken(
        claims: AccessTokenClaims,
    ): AccessToken {
        val signingKey = accessTokenSigningKeyResolver.resolve()

        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.accessTokenPolicy.expirationSeconds)

        val jwt = Jwts.builder()
            .header()
            .keyId(signingKey.keyId)
            .type("JWT")
            .and()
            .id(TsidGenerator.generate().toString())
            .subject(claims.subject)
            .issuer(policy.accessTokenPolicy.issuer)
            .audience().add(claims.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("authorities", claims.authorities)
            .signWith(signingKey.signingKey)
            .compact()
        return AccessToken(jwt)
    }

    override fun generateRefreshToken(
        claims: RefreshTokenClaims
    ): RefreshToken {
        val refreshTokenKey = refreshTokenKeyResolver.resolve()

        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.refreshTokenPolicy.expirationSeconds)

        val jwt = Jwts.builder()
            .header()
            .keyId(refreshTokenKey.keyId)
            .type("JWT")
            .and()
            .id(TsidGenerator.generate().toString())
            .subject(claims.subject)
            .issuer(policy.refreshTokenPolicy.issuer)
            .audience().add(claims.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(refreshTokenKey.secretKey)
            .compact()
        return RefreshToken(jwt)
    }

}