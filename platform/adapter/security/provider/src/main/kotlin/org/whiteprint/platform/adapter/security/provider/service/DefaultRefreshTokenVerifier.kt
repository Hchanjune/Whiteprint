package org.whiteprint.platform.adapter.security.provider.service

import org.whiteprint.platform.adapter.security.provider.model.RefreshTokenClaims
import org.whiteprint.platform.adapter.security.verifier.policy.JwtException
import org.whiteprint.platform.adapter.security.verifier.policy.TokenPolicy
import org.whiteprint.platform.adapter.security.verifier.policy.RevocationChecker
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import org.whiteprint.platform.adapter.security.provider.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.policy.RefreshTokenKeyResolver

class DefaultRefreshTokenVerifier (
    private val keyResolver: RefreshTokenKeyResolver,
    private val revocationChecker: RevocationChecker
): RefreshTokenVerifier {

    override fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims {
        return try {
            val claims = Jwts.parser()
                .keyLocator { header: Header ->
                    val kid = header["kid"] as? String
                        ?: throw JwtException(TokenPolicy.TOKEN_KEY_ID_MISSING)
                    keyResolver.resolve(kid).secretKey
                }
                .build()
                .parseSignedClaims(token.value)
                .payload

            checkRevocation(claims)

            RefreshTokenClaims(
                subject = claims.subject,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = claims.issuedAt.toInstant(),
                expiresAt = claims.expiration.toInstant(),
            )
        } catch (exception: Exception) {
            throw JwtException.mapFrom(exception)
        }
    }

    private fun checkRevocation(claims: Claims) {
        val lastRevokedAt = revocationChecker.getLastRevokedAt(claims.subject) ?: return
        if (claims.issuedAt.toInstant().isBefore(lastRevokedAt)) {
            throw JwtException(TokenPolicy.TOKEN_NEEDS_UPDATE)
        }
    }

}