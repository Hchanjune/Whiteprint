package org.whiteprint.platform.infra.cache.redis.operation

import org.springframework.data.redis.core.RedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.SetOperations

class RedisSetOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
): SetOperations {
    override fun membersRaw(key: CacheKey): Set<Any> {
        return redisTemplate.opsForSet().members(key.value) ?: emptySet()
    }

    override fun add(key: CacheKey, value: Any) {
        redisTemplate.opsForSet().add(key.value, value)
    }

    override fun isMember(key: CacheKey, value: Any): Boolean {
        return redisTemplate.opsForSet().isMember(key.value, value) ?: false
    }

    override fun remove(key: CacheKey, value: Any): Boolean {
        val result = redisTemplate.opsForSet().remove(key.value, value)
        return result != null && result > 0L
    }
}