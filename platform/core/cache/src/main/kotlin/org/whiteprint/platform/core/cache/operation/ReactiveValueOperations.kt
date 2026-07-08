package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface ReactiveValueOperations {

    suspend fun raw(key: CacheKey): Any?

    suspend fun set(key: CacheKey, value: Any)

    suspend fun setWithTtl(key: CacheKey, value: Any, ttl: Duration)

    suspend fun setIfAbsent(key: CacheKey, value: Any): Boolean

    suspend fun setIfAbsentWithTtl(key: CacheKey, value: Any, ttl: Duration): Boolean

    suspend fun delete(key: CacheKey): Boolean

    suspend fun exists(key: CacheKey): Boolean

    suspend fun expire(key: CacheKey, ttl: Duration): Boolean

}
