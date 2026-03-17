package com.hc.core.cache.operation

import com.hc.core.cache.model.CacheKey

interface SetOperations {
    fun membersRaw(key: CacheKey): Set<Any>
    fun add(key: CacheKey, value: Any)
    fun isMember(key: CacheKey, value: Any): Boolean
    fun remove(key: CacheKey, value: Any): Boolean
}