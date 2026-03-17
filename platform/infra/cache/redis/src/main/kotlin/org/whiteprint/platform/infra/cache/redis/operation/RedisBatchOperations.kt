package org.whiteprint.platform.infra.cache.redis.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheValidator
import com.hc.core.cache.operation.BatchOperations
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class RedisBatchOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
): BatchOperations {

    override fun multiGetRaw(keys: List<CacheKey>): List<Any?> {
        if (keys.isEmpty()) return emptyList()
        return redisTemplate.opsForValue().multiGet(keys.map { it.value }) ?: keys.map { null }
    }

    override fun <T : Any> multiSet(map: Map<CacheKey, T>) {
        if (map.isEmpty()) return
        redisTemplate.opsForValue().multiSet(map.mapKeys { it.key.value })
    }

    override fun multiDelete(keys: List<CacheKey>) {
        if (keys.isEmpty()) return
        redisTemplate.delete(keys.map { it.value })
    }

    override fun multiExpire(keys: List<CacheKey>, ttl: Duration) {
        if (keys.isEmpty()) return
        CacheValidator.validateTtlOrThrow(ttl)
        redisTemplate.executePipelined {
            keys.forEach { key ->
                redisTemplate.expire(key.value, ttl)
            }
        }
    }

    override fun <T : Any> multiSetAndExpire(
        map: Map<CacheKey, T>,
        ttl: Duration
    ) {
        if (map.isEmpty()) return
        CacheValidator.validateTtlOrThrow(ttl)
        redisTemplate.executePipelined {
            map.forEach { (key, value) ->
                redisTemplate.opsForValue().set(key.value, value, ttl)
            }
            null
        }
    }

}