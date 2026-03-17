package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface ValueOperations {

    fun raw(key: org.whiteprint.platform.core.cache.model.CacheKey): Any?

    fun set(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any)

    fun setWithTtl(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any, ttl: Duration)

    fun setIfAbsent(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any): Boolean

    fun setIfAbsentWithTtl(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any, ttl: Duration): Boolean

    fun delete(key: org.whiteprint.platform.core.cache.model.CacheKey): Boolean

    fun exists(key: org.whiteprint.platform.core.cache.model.CacheKey): Boolean

    fun expire(key: org.whiteprint.platform.core.cache.model.CacheKey, ttl: Duration): Boolean

}