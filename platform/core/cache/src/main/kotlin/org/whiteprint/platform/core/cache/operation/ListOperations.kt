package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey

interface ListOperations {
    fun leftPush(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any)
    fun rightPopRaw(key: org.whiteprint.platform.core.cache.model.CacheKey): Any?
    fun size(key: org.whiteprint.platform.core.cache.model.CacheKey): Long
    fun rangeRaw(key: org.whiteprint.platform.core.cache.model.CacheKey, start: Long, end: Long): List<Any>
}