package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy

suspend inline fun <reified T : Any> ReactiveListOperations.rightPop(key: CacheKey): T? {
    val value = this.rightPopRaw(key) ?: return null
    if (value !is T) {
        throw CacheException(
            policy = CachePolicy.CLASS_CAST_FAILED,
            attributes = mapOf("key" to key.value, "expectedType" to (T::class.simpleName ?: "unknown"))
        )
    }
    return value
}
