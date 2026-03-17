package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface AtomicOperations {
    fun incrementOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L): Long
    fun incrementAndExpireOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, ttl: Duration, delta: Long = 1L): Long

    fun incrementWithLimitOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L, limit: Long): Long
    fun incrementWithLimitAndExpireOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long

    fun decrementOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L): Long
    fun decrementAndExpireOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, ttl: Duration, delta: Long = 1L): Long

    fun decrementWithLimitOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L, limit: Long): Long
    fun decrementWithLimitAndExpireOrThrow(key: org.whiteprint.platform.core.cache.model.CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long
}