package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey

interface SetOperations {
    fun membersRaw(key: CacheKey): Set<Any>
    fun add(key:CacheKey, value: Any)
    fun isMember(key: CacheKey, value: Any): Boolean
    fun remove(key: CacheKey, value: Any): Boolean
}