package org.whiteprint.platform.infra.cache.redis.reactive.operation

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CacheValidator
import org.whiteprint.platform.core.cache.operation.ReactiveValueOperations
import java.time.Duration

class RedisReactiveValueOperations(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
): ReactiveValueOperations {

    override suspend fun raw(key: CacheKey): Any? =
        redisTemplate.opsForValue().get(key.value).awaitSingleOrNull()

    override suspend fun set(key: CacheKey, value: Any) {
        redisTemplate.opsForValue().set(key.value, value).awaitSingleOrNull()
    }

    override suspend fun setWithTtl(key: CacheKey, value: Any, ttl: Duration) {
        CacheValidator.validateTtlOrThrow(ttl)
        redisTemplate.opsForValue().set(key.value, value, ttl).awaitSingleOrNull()
    }

    override suspend fun setIfAbsent(key: CacheKey, value: Any): Boolean =
        redisTemplate.opsForValue().setIfAbsent(key.value, value).awaitSingleOrNull() ?: false

    override suspend fun setIfAbsentWithTtl(key: CacheKey, value: Any, ttl: Duration): Boolean {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.opsForValue().setIfAbsent(key.value, value, ttl).awaitSingleOrNull() ?: false
    }

    override suspend fun delete(key: CacheKey): Boolean =
        ((redisTemplate.delete(key.value).awaitSingleOrNull()) ?: 0L) > 0L

    override suspend fun exists(key: CacheKey): Boolean =
        redisTemplate.hasKey(key.value).awaitSingleOrNull() ?: false

    override suspend fun expire(key: CacheKey, ttl: Duration): Boolean {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.expire(key.value, ttl).awaitSingleOrNull() ?: false
    }

}
