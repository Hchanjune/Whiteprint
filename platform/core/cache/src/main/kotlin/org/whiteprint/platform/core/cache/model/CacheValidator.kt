package org.whiteprint.platform.core.cache.model

import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import java.time.Duration

object CacheValidator {
    fun validateTtlOrThrow(ttl: Duration) {
        if (ttl.isZero || ttl.isNegative) {
            throw CacheException(
                policy = CachePolicy.TTL_MUST_BE_POSITIVE,
                attributes = mapOf(
                    "ttl" to ttl
                )
            )
        }
    }
}