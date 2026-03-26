package org.whiteprint.platform.infra.security.jwt.verifier

import io.jsonwebtoken.Jwts
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.platform.infra.security.jwt.policy.JwtExceptionMapper

class JwtRefreshTokenVerifier (
    private val keyResolver: RefreshTokenKeyResolver,
    private val revocationChecker: RevocationChecker
): RefreshTokenVerifier {

    override fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims {
        return try {
            val secretKey = keyResolver.resolve().secretKey
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token.value)
                .payload

            val refreshTokenClaims = RefreshTokenClaims(
                tokenId = claims.id,
                subject = claims.subject,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = claims.issuedAt.toInstant(),
                expiresAt = claims.expiration.toInstant(),
            )
            revocationChecker.assertNotRevoked(refreshTokenClaims)

            refreshTokenClaims

        } catch (exception: Exception) {
            throw JwtExceptionMapper.mapFrom(exception)
        }
    }

}