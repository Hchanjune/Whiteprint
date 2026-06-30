package org.whiteprint.platform.adapter.security.verifier.reactive.security

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.verifier.RevocationValue
import org.whiteprint.platform.core.security.verifier.TokenRevoker
import java.time.Duration
import java.time.Instant

class TokenRevokerImpl(
    private val cache: ValueOperations,
    private val keyStrategy: SecurityCacheKeyStrategy,
    private val servicePrefix: String = "",
) : TokenRevoker {

    override fun revokeToken(tokenId: String, reason: RevocationReason, duration: Duration) {
        val value = RevocationValue.Token(reason).serialize()
        cache.setWithTtl(
            key = CacheKey(keyStrategy.revocationToken(tokenId, servicePrefix)),
            value = value,
            ttl = duration
        )
    }

    override fun revokeAccount(subject: String, reason: RevocationReason, duration: Duration) {
        val value = RevocationValue.Account(reason, Instant.now().toEpochMilli()).serialize()
        cache.setWithTtl(
            key = CacheKey(keyStrategy.revocationAccount(subject, servicePrefix)),
            value = value,
            ttl = duration
        )
    }
}
