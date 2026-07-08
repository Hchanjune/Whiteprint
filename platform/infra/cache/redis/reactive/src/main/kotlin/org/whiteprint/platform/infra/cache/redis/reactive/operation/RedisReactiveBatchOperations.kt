package org.whiteprint.platform.infra.cache.redis.reactive.operation

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CacheValidator
import org.whiteprint.platform.core.cache.operation.ReactiveBatchOperations
import java.time.Duration

/**
 * blocking 쪽 executePipelined(단일 라운드트립 배칭)에 대응하는 reactive 개념이 없어서
 * 여기서는 명령을 순차적으로 보낸다 — 결과는 동일하지만 배칭 최적화는 없다.
 */
class RedisReactiveBatchOperations(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
): ReactiveBatchOperations {

    override suspend fun multiGetRaw(keys: List<CacheKey>): List<Any?> {
        if (keys.isEmpty()) return emptyList()
        return redisTemplate.opsForValue().multiGet(keys.map { it.value }).awaitSingleOrNull() ?: keys.map { null }
    }

    override suspend fun <T : Any> multiSet(map: Map<CacheKey, T>) {
        if (map.isEmpty()) return
        redisTemplate.opsForValue().multiSet(map.mapKeys { it.key.value }).awaitSingleOrNull()
    }

    override suspend fun multiDelete(keys: List<CacheKey>) {
        if (keys.isEmpty()) return
        redisTemplate.delete(*keys.map { it.value }.toTypedArray()).awaitSingleOrNull()
    }

    override suspend fun multiExpire(keys: List<CacheKey>, ttl: Duration) {
        if (keys.isEmpty()) return
        CacheValidator.validateTtlOrThrow(ttl)
        keys.forEach { key -> redisTemplate.expire(key.value, ttl).awaitSingleOrNull() }
    }

    override suspend fun <T : Any> multiSetAndExpire(map: Map<CacheKey, T>, ttl: Duration) {
        if (map.isEmpty()) return
        CacheValidator.validateTtlOrThrow(ttl)
        map.forEach { (key, value) -> redisTemplate.opsForValue().set(key.value, value, ttl).awaitSingleOrNull() }
    }

}
