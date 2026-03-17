package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy

inline fun <reified T : Any> SetOperations.members(key: CacheKey): Set<T> {
    return this.membersRaw(key).map { value ->
        if (value !is T) {
            throw CacheException(
                policy = CachePolicy.CLASS_CAST_FAILED,
                attributes = mapOf("key" to key.value, "expectedType" to (T::class.simpleName ?: "unknown"))
            )
        }
        value as T
    }.toSet()
}