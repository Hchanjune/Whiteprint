package org.whiteprint.platform.infra.cache.redis.reactive.operation

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ReactiveListOperations

class RedisReactiveListOperations(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
): ReactiveListOperations {

    override suspend fun leftPush(key: CacheKey, value: Any) {
        redisTemplate.opsForList().leftPush(key.value, value).awaitSingleOrNull()
    }

    override suspend fun rightPopRaw(key: CacheKey): Any? =
        redisTemplate.opsForList().rightPop(key.value).awaitSingleOrNull()

    override suspend fun size(key: CacheKey): Long =
        redisTemplate.opsForList().size(key.value).awaitSingleOrNull() ?: 0L

    override suspend fun rangeRaw(key: CacheKey, start: Long, end: Long): List<Any> =
        redisTemplate.opsForList().range(key.value, start, end).collectList().awaitSingleOrNull() ?: emptyList()

}
