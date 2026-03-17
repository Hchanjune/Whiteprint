package org.whiteprint.platform.core.cache.model

data class CacheLockHandle (
    val key: org.whiteprint.platform.core.cache.model.CacheKey,
    val owner: String,
    val fencingToken: Long
)