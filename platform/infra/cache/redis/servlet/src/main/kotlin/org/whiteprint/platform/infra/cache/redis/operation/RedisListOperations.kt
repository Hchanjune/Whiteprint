package org.whiteprint.platform.infra.cache.redis.operation

import org.springframework.data.redis.core.RedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ListOperations

class RedisListOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
): ListOperations {

    override fun leftPush(key: CacheKey, value: Any) {
        redisTemplate.opsForList().leftPush(key.value, value)
    }

    override fun rightPopRaw(key: CacheKey): Any? {
        return redisTemplate.opsForList().rightPop(key.value)
    }

    override fun size(key: CacheKey): Long {
        return redisTemplate.opsForList().size(key.value) ?: 0L
    }

    override fun rangeRaw(
        key: CacheKey,
        start: Long,
        end: Long
    ): List<Any> {
        return redisTemplate.opsForList().range(key.value, start, end) ?: emptyList()
    }
}