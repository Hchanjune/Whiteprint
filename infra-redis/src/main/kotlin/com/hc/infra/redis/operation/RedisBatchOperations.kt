package com.hc.infra.redis.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.operation.BatchOperations
import com.hc.infra.redis.client.RedisClient
import java.time.Duration

class RedisBatchOperations(
    private val client: RedisClient,
): BatchOperations {

    override fun multiGetRaw(keys: List<CacheKey>): List<Any?> {
        return client.multiGetRaw(keys)
    }

    override fun <T : Any> multiSet(map: Map<CacheKey, T>) {
        client.multiSet(map)
    }

    override fun multiDelete(keys: List<CacheKey>) {
        client.multiDelete(keys)
    }

    override fun multiExpire(keys: List<CacheKey>, ttl: Duration) {
        if (keys.isEmpty()) return
        client.validateTtlOrThrow(ttl)
        client.executePipelined {
            keys.forEach { key ->
                client.expire(key, ttl)
            }
        }
    }

    override fun <T : Any> multiSetAndExpire(
        map: Map<CacheKey, T>,
        ttl: Duration
    ) {
        if (map.isEmpty()) return
        client.validateTtlOrThrow(ttl)
        client.executePipelined {
            map.forEach { (key, value) ->
                client.set(key, value)
            }
        }
    }

}