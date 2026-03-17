package com.hc.core.cache.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.policy.CacheException
import com.hc.core.cache.policy.CachePolicy

inline fun <reified T : Any> ListOperations.rightPop(key: CacheKey): T? {
    val value = this.rightPopRaw(key) ?: return null
    if (value !is T) {
        throw CacheException(
            policy = CachePolicy.CLASS_CAST_FAILED,
            attributes = mapOf("key" to key.value, "expectedType" to (T::class.simpleName ?: "unknown"))
        )
    }
    return value as T
}