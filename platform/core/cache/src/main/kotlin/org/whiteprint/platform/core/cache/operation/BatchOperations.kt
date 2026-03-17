package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface BatchOperations {

    fun multiGetRaw(keys: List<CacheKey>): List<Any?>

    fun <T: Any> multiSet(map: Map<CacheKey, T>)

    fun multiDelete(keys: List<CacheKey>)

    fun multiExpire(keys: List<CacheKey>, ttl: Duration)

    fun <T: Any> multiSetAndExpire(map: Map<CacheKey, T>, ttl: Duration)

}