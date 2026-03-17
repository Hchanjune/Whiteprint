package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface ValueOperations {

    fun raw(key: CacheKey): Any?

    fun set(key: CacheKey, value: Any)

    fun setWithTtl(key: CacheKey, value: Any, ttl: Duration)

    fun setIfAbsent(key: CacheKey, value: Any): Boolean

    fun setIfAbsentWithTtl(key: CacheKey, value: Any, ttl: Duration): Boolean

    fun delete(key: CacheKey): Boolean

    fun exists(key: CacheKey): Boolean

    fun expire(key: CacheKey, ttl: Duration): Boolean

}