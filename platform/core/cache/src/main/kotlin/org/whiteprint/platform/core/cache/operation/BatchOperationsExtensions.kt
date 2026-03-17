package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy

inline fun <reified T : Any> BatchOperations.multiGet(keys: List<CacheKey>): List<T?> {
    val rawValues = this.multiGetRaw(keys)

    return keys.zip(rawValues).map { (key, value) ->
        if (value != null && value !is T) {
            throw CacheException(
                policy = CachePolicy.CLASS_CAST_FAILED,
                attributes = mapOf(
                    "key" to key.value,
                    "expectedType" to (T::class.simpleName ?: "unknown"),
                    "actualType" to (value::class.simpleName ?: "unknown")
                )
            )
        }
        value as T?
    }
}

inline fun <reified T : Any> BatchOperations.multiGetOrThrow(keys: List<CacheKey>): List<T> {
    val rawValues = this.multiGetRaw(keys)

    return keys.zip(rawValues).map { (key, value) ->
        if (value == null) {
            throw CacheException(
                policy = CachePolicy.REQUIRED_KEY_NOT_FOUND,
                attributes = mapOf("key" to key.value)
            )
        }

        if (value !is T) {
            throw CacheException(
                policy = CachePolicy.CLASS_CAST_FAILED,
                attributes = mapOf(
                    "key" to key.value,
                    "expectedType" to (T::class.simpleName ?: "unknown"),
                    "actualType" to (value::class.simpleName ?: "unknown")
                )
            )
        }
        value as T
    }
}