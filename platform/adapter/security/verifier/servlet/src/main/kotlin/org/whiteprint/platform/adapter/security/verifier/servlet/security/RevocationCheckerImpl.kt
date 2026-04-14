package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.operation.get
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.model.TokenClaims
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.verifier.RevocationChecker

class RevocationCheckerImpl(
    private val cache: ValueOperations,
    private val revocationKeyStrategy: SecurityCacheKeyStrategy,
    private val servicePrefix: String = ""
): RevocationChecker {

    override fun assertNotRevoked(claims: AccessTokenClaims) = check(claims)

    override fun assertNotRevoked(claims: RefreshTokenClaims) = check(claims)

    private fun check(claims: TokenClaims) {
        // TokenKey
        verifyKey(revocationKeyStrategy.revocationToken(claims.tokenId, servicePrefix))

        // UserKey
        verifyKey(revocationKeyStrategy.revocationAccount(claims.subject, servicePrefix))
    }

    private fun verifyKey(key: String) {
        cache.get<String>(CacheKey(key))?.let { value ->
            val reason = RevocationReason.valueOf(value)
            throw SecurityException(policy = reason.toPolicy())
        }
    }

}