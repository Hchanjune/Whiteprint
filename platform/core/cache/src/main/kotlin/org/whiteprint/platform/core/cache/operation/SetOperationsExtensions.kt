package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy

inline fun <reified T : Any> org.whiteprint.platform.core.cache.operation.SetOperations.members(key: org.whiteprint.platform.core.cache.model.CacheKey): Set<T> {
    return this.membersRaw(key).map { value ->
        if (value !is T) {
            throw _root_ide_package_.org.whiteprint.platform.core.cache.policy.CacheException(
                policy = _root_ide_package_.org.whiteprint.platform.core.cache.policy.CachePolicy.CLASS_CAST_FAILED,
                attributes = mapOf("key" to key.value, "expectedType" to (T::class.simpleName ?: "unknown"))
            )
        }
        value as T
    }.toSet()
}