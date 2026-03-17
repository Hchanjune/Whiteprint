package com.hc.infra.cache.redis.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheValidator
import com.hc.core.cache.operation.ValueOperations
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class RedisValueOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
): ValueOperations {

    override fun raw(key: CacheKey): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    override fun set(key: CacheKey, value: Any) {
        redisTemplate.opsForValue().set(key.value, value)
    }

    override fun setWithTtl(key: CacheKey, value: Any, ttl: Duration) {
        CacheValidator.validateTtlOrThrow(ttl)
        redisTemplate.opsForValue().set(key.value, value, ttl)
    }

    override fun setIfAbsent(key: CacheKey, value: Any): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(key.value, value)?: false
    }

    override fun setIfAbsentWithTtl(
        key: CacheKey,
        value: Any,
        ttl: Duration
    ): Boolean {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.opsForValue().setIfAbsent(key.value, value, ttl)?: false
    }

    override fun delete(key: CacheKey): Boolean {
        return redisTemplate.delete(key.value)?: false
    }

    override fun exists(key: CacheKey): Boolean {
        return redisTemplate.hasKey(key.value) ?: false
    }

    override fun expire(key: CacheKey, ttl: Duration): Boolean {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.expire(key.value, ttl)?: false
    }

}