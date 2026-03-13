package com.hc.infra.redis.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.operation.ListOperations
import org.springframework.data.redis.core.RedisTemplate

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