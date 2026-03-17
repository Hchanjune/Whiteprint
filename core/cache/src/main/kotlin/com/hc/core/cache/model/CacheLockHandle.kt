package com.hc.core.cache.model

data class CacheLockHandle (
    val key: CacheKey,
    val owner: String,
    val fencingToken: Long
)