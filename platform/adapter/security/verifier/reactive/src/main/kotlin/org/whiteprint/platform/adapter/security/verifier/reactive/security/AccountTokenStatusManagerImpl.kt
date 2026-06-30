package org.whiteprint.platform.adapter.security.verifier.reactive.security

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.operation.get
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.verifier.AccountTokenStatusManager
import java.time.Duration

class AccountTokenStatusManagerImpl(
    private val cache: ValueOperations,
    private val keyStrategy: SecurityCacheKeyStrategy,
    private val servicePrefix: String = "",
    private val forceUpdateExpiration: Duration,
) : AccountTokenStatusManager {

    override fun setForceUpdate(subject: String, updatedAt: Long) {
        cache.setWithTtl(
            key = CacheKey(keyStrategy.forceUpdate(subject, servicePrefix)),
            value = updatedAt,
            ttl = forceUpdateExpiration
        )
    }

    override fun checkForceUpdate(subject: String, issuedAt: Long): Boolean {
        val updatedAt = cache.get<Long>(CacheKey(keyStrategy.forceUpdate(subject, servicePrefix)))
            ?: return false
        return issuedAt < updatedAt
    }
}
