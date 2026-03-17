package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey

interface SetOperations {
    fun membersRaw(key: org.whiteprint.platform.core.cache.model.CacheKey): Set<Any>
    fun add(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any)
    fun isMember(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any): Boolean
    fun remove(key: org.whiteprint.platform.core.cache.model.CacheKey, value: Any): Boolean
}