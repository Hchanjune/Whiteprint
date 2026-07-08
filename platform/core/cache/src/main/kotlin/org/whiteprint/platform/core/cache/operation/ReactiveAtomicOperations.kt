package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface ReactiveAtomicOperations {
    suspend fun incrementOrThrow(key: CacheKey, delta: Long = 1L): Long
    suspend fun incrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long = 1L): Long

    suspend fun incrementWithLimitOrThrow(key: CacheKey, delta: Long = 1L, limit: Long): Long
    suspend fun incrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long

    suspend fun decrementOrThrow(key: CacheKey, delta: Long = 1L): Long
    suspend fun decrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long = 1L): Long

    suspend fun decrementWithLimitOrThrow(key: CacheKey, delta: Long = 1L, limit: Long): Long
    suspend fun decrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long = 1L, limit: Long, ttl: Duration): Long
}
