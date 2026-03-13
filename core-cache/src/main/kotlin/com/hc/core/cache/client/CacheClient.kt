package com.hc.core.cache.client

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheScript
import java.time.Duration

interface CacheClient {

    fun raw(key: CacheKey): Any?

    fun set(key: CacheKey, value: Any)

    fun setWithTtl(key: CacheKey, value: Any, ttl: Duration)

    fun setIfAbsent(key: CacheKey, value: Any): Boolean

    fun setIfAbsentWithTtl(key: CacheKey, value: Any, ttl: Duration): Boolean

    fun delete(key: CacheKey): Boolean

    fun exists(key: CacheKey): Boolean

    fun expire(key: CacheKey, ttl: Duration): Boolean

    fun validateTtlOrThrow(ttl: Duration)

    fun <T : Any> executeScript(
        script: CacheScript<T>,
        keys: List<CacheKey>,
        vararg args: Any
    ): T?

    fun incrementOrThrow(key: CacheKey, delta: Long): Long

    fun decrementOrThrow(key: CacheKey, delta: Long): Long

    fun multiGetRaw(keys: List<CacheKey>): List<Any?>

    fun multiSet(map: Map<CacheKey, Any>)

    fun multiDelete(keys: List<CacheKey>)

    fun executePipelined(action: () -> Unit)

}