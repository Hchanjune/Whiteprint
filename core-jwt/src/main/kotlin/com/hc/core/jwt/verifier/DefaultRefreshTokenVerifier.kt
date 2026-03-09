package com.hc.core.jwt.verifier

import com.hc.core.jwt.exception.JwtException
import com.hc.core.jwt.model.RefreshToken
import com.hc.core.jwt.model.RefreshTokenClaims
import com.hc.core.jwt.model.RefreshTokenKey
import com.hc.core.jwt.model.TokenSubjects
import com.hc.core.jwt.policy.RefreshTokenPolicy
import io.jsonwebtoken.Jwts

class DefaultRefreshTokenVerifier (
    private val refreshTokenPolicy: RefreshTokenPolicy,
    private val refreshTokenKey: RefreshTokenKey,
    private val revocationChecker: RevocationChecker
): RefreshTokenVerifier {

    override fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims {
        return try {
            val claims = Jwts.parser()
                .verifyWith(refreshTokenKey.value)
                .requireSubject(TokenSubjects.REFRESH.name)
                .requireIssuer(refreshTokenPolicy.issuer)
                .build()
                .parseSignedClaims(token.value)
                .payload

            val userId = claims["userId"] as String
            val issuedAt = claims.issuedAt.toInstant()

            revocationChecker.getLastRevokedAt(userId)?.let { lastRevokedAt ->
                if (issuedAt.isBefore(lastRevokedAt)) throw JwtException.RefreshTokenBlacklistedException()
            }

            RefreshTokenClaims(
                subject = TokenSubjects.REFRESH,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = issuedAt,
                expiresAt = claims.expiration.toInstant(),
                userId = userId
            )
        } catch (exception: Exception) {
            throw JwtException.mapFrom(exception)
        }
    }

}