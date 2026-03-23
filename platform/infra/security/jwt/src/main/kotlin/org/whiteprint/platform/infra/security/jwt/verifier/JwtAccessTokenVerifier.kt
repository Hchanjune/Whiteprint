package org.whiteprint.platform.infra.security.jwt.verifier

import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import org.whiteprint.platform.infra.security.jwt.policy.JwtExceptionMapper
import org.whiteprint.platform.core.security.policy.SecurityException

class JwtAccessTokenVerifier (
    private val keyResolver: AccessTokenVerificationKeyResolver,
    private val revocationChecker: RevocationChecker
): AccessTokenVerifier {

    override fun verifyOrThrow(token: AccessToken): AccessTokenClaims {
        return try {
            val claims = Jwts.parser()
                .keyLocator { header: Header ->
                    val kid = header["kid"] as? String
                        ?: throw SecurityException(SecurityPolicy.TOKEN_KEY_ID_MISSING)
                    keyResolver.resolve(kid).verifyKey
                }
                .build()
                .parseSignedClaims(token.value)
                .payload

            val accessTokenClaims = AccessTokenClaims(
                tokenId = claims.id,
                subject = claims.subject,
                issuer = claims.issuer,
                audience = claims.audience,
                issuedAt = claims.issuedAt.toInstant(),
                expiresAt = claims.expiration.toInstant(),
                authorities = (claims["authorities"] as? Iterable<*>)?.map { it.toString() }?.toSet() ?: emptySet()
            )

            revocationChecker.assertNotRevoked(accessTokenClaims)

            accessTokenClaims

        } catch (exception: Exception) {
            throw JwtExceptionMapper.mapFrom(exception)
        }
    }

}