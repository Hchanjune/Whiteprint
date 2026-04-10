package org.whiteprint.platform.infra.security.jwt.verifier

import io.jsonwebtoken.Header
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.platform.infra.security.jwt.policy.JwtExceptionMapper

class JwtRefreshTokenVerifier (
    private val keyResolver: RefreshTokenVerificationKeyResolver,
    private val revocationChecker: RevocationChecker
): RefreshTokenVerifier {

    override fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims {
        return try {
            val claims = Jwts.parser()
                .keyLocator { header: Header ->
                    val kid = header["kid"] as? String
                        ?: throw SecurityException(SecurityPolicy.TOKEN_KEY_ID_MISSING)
                    val ver = header["ver"] as? String
                    println("Debug: $kid")
                    println("Debug: $ver")
                    keyResolver.resolve(kid, ver).verifyKey
                }
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
            exception.printStackTrace()
            if (exception is JwtException) {
                throw JwtExceptionMapper.mapFrom(exception)
            } else {
                throw exception
            }
        }
    }

}