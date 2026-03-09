package com.hc.core.jwt.verifier

import com.hc.core.jwt.exception.JwtException
import com.hc.core.jwt.model.AccessToken
import com.hc.core.jwt.model.AccessTokenClaims
import com.hc.core.jwt.model.AccessTokenKey
import com.hc.core.jwt.model.TokenSubjects
import com.hc.core.jwt.policy.AccessTokenPolicy
import io.jsonwebtoken.Jwts

class DefaultAccessTokenVerifier (
    private val accessTokenPolicy: AccessTokenPolicy,
    private val accessTokenKey: AccessTokenKey,
    private val revocationChecker: RevocationChecker
): AccessTokenVerifier {

    override fun verifyOrThrow(token: AccessToken): AccessTokenClaims {
        return try {
            val claims = Jwts.parser()
                .verifyWith(accessTokenKey.value)
                .requireSubject(TokenSubjects.ACCESS.name)
                .requireIssuer(accessTokenPolicy.issuer)
                .build()
                .parseSignedClaims(token.value)
                .payload

            val userId = claims["userId"] as String
            val issuedAt = claims.issuedAt.toInstant()

            revocationChecker.getLastRevokedAt(userId)?.let { lastRevokedAt ->
                if (issuedAt.isBefore(lastRevokedAt)) throw JwtException.AccessTokenNeedsUpdateException()
            }

            AccessTokenClaims(
                subject = TokenSubjects.ACCESS,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = issuedAt,
                expiresAt = claims.expiration.toInstant(),
                userId = userId,
                authorities = (claims["authorities"] as? Iterable<*>)?.map { it.toString() }?.toSet()?: emptySet()
            )
        } catch (exception: Exception) {
            throw JwtException.mapFrom(exception)
        }
    }

}