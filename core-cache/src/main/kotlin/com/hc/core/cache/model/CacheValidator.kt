package com.hc.core.cache.model

import com.hc.core.cache.policy.CacheException
import com.hc.core.cache.policy.CachePolicy
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