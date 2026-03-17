package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface AtomicOperations {
    fun incrementOrThrow(key: CacheKey, delta: Long = 1L): Long
    fun incrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long = 1L): Long

    fun incrementWithLimitOrThrow(key: CacheKey, delta: Long = 1L, limit: Long): Long
    fun incrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long

    fun decrementOrThrow(key: CacheKey, delta: Long = 1L): Long
    fun decrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long = 1L): Long

    fun decrementWithLimitOrThrow(key: CacheKey, delta: Long = 1L, limit: Long): Long
    fun decrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long
}