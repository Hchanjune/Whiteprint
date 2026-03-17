package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface BatchOperations {

    fun multiGetRaw(keys: List<org.whiteprint.platform.core.cache.model.CacheKey>): List<Any?>

    fun <T: Any> multiSet(map: Map<org.whiteprint.platform.core.cache.model.CacheKey, T>)

    fun multiDelete(keys: List<org.whiteprint.platform.core.cache.model.CacheKey>)

    fun multiExpire(keys: List<org.whiteprint.platform.core.cache.model.CacheKey>, ttl: Duration)

    fun <T: Any> multiSetAndExpire(map: Map<org.whiteprint.platform.core.cache.model.CacheKey, T>, ttl: Duration)

}