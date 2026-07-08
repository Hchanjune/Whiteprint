package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy

suspend inline fun <reified T: Any> ReactiveValueOperations.get(key: CacheKey): T? {
    val value = this.raw(key) ?: return null
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
    return value
}

suspend inline fun <reified T> ReactiveValueOperations.getOrThrow(key: CacheKey): T {
    val value = this.raw(key)
        ?: throw CacheException(
            policy = CachePolicy.REQUIRED_KEY_NOT_FOUND,
            attributes = mapOf("key" to key.value)
        )

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

    return value
}
