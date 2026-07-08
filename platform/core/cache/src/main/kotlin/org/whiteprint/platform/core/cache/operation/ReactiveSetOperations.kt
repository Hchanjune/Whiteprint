package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey

interface ReactiveSetOperations {
    suspend fun membersRaw(key: CacheKey): Set<Any>
    suspend fun add(key: CacheKey, value: Any)
    suspend fun isMember(key: CacheKey, value: Any): Boolean
    suspend fun remove(key: CacheKey, value: Any): Boolean
}
