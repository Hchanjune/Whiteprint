package org.whiteprint.platform.infra.cache.redis.reactive.operation

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ReactiveSetOperations

class RedisReactiveSetOperations(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
): ReactiveSetOperations {

    override suspend fun membersRaw(key: CacheKey): Set<Any> =
        redisTemplate.opsForSet().members(key.value).collectList().awaitSingleOrNull()?.toSet() ?: emptySet()

    override suspend fun add(key: CacheKey, value: Any) {
        redisTemplate.opsForSet().add(key.value, value).awaitSingleOrNull()
    }

    override suspend fun isMember(key: CacheKey, value: Any): Boolean =
        redisTemplate.opsForSet().isMember(key.value, value).awaitSingleOrNull() ?: false

    override suspend fun remove(key: CacheKey, value: Any): Boolean {
        val result = redisTemplate.opsForSet().remove(key.value, value).awaitSingleOrNull()
        return result != null && result > 0L
    }

}
