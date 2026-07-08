package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import java.time.Duration

interface ReactiveBatchOperations {

    suspend fun multiGetRaw(keys: List<CacheKey>): List<Any?>

    suspend fun <T: Any> multiSet(map: Map<CacheKey, T>)

    suspend fun multiDelete(keys: List<CacheKey>)

    suspend fun multiExpire(keys: List<CacheKey>, ttl: Duration)

    suspend fun <T: Any> multiSetAndExpire(map: Map<CacheKey, T>, ttl: Duration)

}
