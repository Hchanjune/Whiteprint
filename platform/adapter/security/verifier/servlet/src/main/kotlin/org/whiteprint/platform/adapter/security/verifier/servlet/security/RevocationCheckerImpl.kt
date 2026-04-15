package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.operation.get
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.model.TokenClaims
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.verifier.RevocationValue
import java.time.Instant

class RevocationCheckerImpl(
    private val cache: ValueOperations,
    private val revocationKeyStrategy: SecurityCacheKeyStrategy,
    private val servicePrefix: String = ""
): RevocationChecker {

    override fun assertNotRevoked(claims: AccessTokenClaims) = check(claims)

    override fun assertNotRevoked(claims: RefreshTokenClaims) = check(claims)

    private fun check(claims: TokenClaims) {
        verifyTokenKey(revocationKeyStrategy.revocationToken(claims.tokenId, servicePrefix))
        verifyAccountKey(revocationKeyStrategy.revocationAccount(claims.subject, servicePrefix), claims.issuedAt)
    }

    private fun verifyTokenKey(key: String) {
        cache.get<String>(CacheKey(key))?.let { value ->
            val revocation = RevocationValue.deserializeToken(value)
            throw SecurityException(policy = revocation.reason.toPolicy())
        }
    }

    private fun verifyAccountKey(key: String, issuedAt: Instant) {
        cache.get<String>(CacheKey(key))?.let { value ->
            val revocation = RevocationValue.deserializeAccount(value)
            if (issuedAt.toEpochMilli() < revocation.revokedAt) {
                throw SecurityException(policy = revocation.reason.toPolicy())
            }
        }
    }

}