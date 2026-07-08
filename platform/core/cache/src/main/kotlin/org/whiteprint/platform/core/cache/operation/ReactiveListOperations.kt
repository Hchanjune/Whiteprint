package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey

interface ReactiveListOperations {
    suspend fun leftPush(key: CacheKey, value: Any)
    suspend fun rightPopRaw(key: CacheKey): Any?
    suspend fun size(key: CacheKey): Long
    suspend fun rangeRaw(key: CacheKey, start: Long, end: Long): List<Any>
}
