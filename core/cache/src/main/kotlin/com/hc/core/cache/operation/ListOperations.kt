package com.hc.core.cache.operation

import com.hc.core.cache.model.CacheKey

interface ListOperations {
    fun leftPush(key: CacheKey, value: Any)
    fun rightPopRaw(key: CacheKey): Any?
    fun size(key: CacheKey): Long
    fun rangeRaw(key: CacheKey, start: Long, end: Long): List<Any>
}