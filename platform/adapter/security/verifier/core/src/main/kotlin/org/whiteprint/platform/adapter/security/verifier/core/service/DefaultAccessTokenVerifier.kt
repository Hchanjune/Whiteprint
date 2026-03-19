package org.whiteprint.platform.adapter.security.verifier.core.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenClaims
import org.whiteprint.platform.adapter.security.verifier.core.policy.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.adapter.security.verifier.core.policy.JwtException
import org.whiteprint.platform.adapter.security.verifier.core.policy.RevocationChecker
import org.whiteprint.platform.adapter.security.verifier.core.policy.TokenPolicy

class DefaultAccessTokenVerifier (
    private val keyResolver: AccessTokenVerificationKeyResolver,
    private val revocationChecker: RevocationChecker
): AccessTokenVerifier {

    override fun verifyOrThrow(token: AccessToken): AccessTokenClaims {
        return try {
            val claims = Jwts.parser()
                .keyLocator { header: Header ->
                    val kid = header["kid"] as? String
                        ?: throw JwtException(TokenPolicy.TOKEN_KEY_ID_MISSING)
                    keyResolver.resolve(kid).verifyKey
                }
                .build()
                .parseSignedClaims(token.value)
                .payload

            checkRevocation(claims)

            AccessTokenClaims(
                subject = claims.subject,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = claims.issuedAt.toInstant(),
                expiresAt = claims.expiration.toInstant(),
                authorities = (claims["authorities"] as? Iterable<*>)?.map { it.toString() }?.toSet() ?: emptySet()
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