package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.provider.TokenRevoker
import java.time.Duration

class TokenRevokerImpl(
    private val cache: ValueOperations,
    private val revocationKeyStrategy: SecurityCacheKeyStrategy,
    private val servicePrefix: String = ""
): TokenRevoker {

    override fun revokeToken(
        tokenId: String,
        reason: RevocationReason,
        duration: Duration
    ) {
        cache.setWithTtl(
            key = CacheKey(revocationKeyStrategy.revocationToken(tokenId, servicePrefix)),
            value = reason.name,
            ttl = duration
        )
    }

    override fun revokeAccount(
        subject: String,
        reason: RevocationReason,
        duration: Duration
    ) {
        cache.setWithTtl(
            key = CacheKey(revocationKeyStrategy.revocationAccount(subject, servicePrefix)),
            value = reason.name,
            ttl = duration
        )
    }

}