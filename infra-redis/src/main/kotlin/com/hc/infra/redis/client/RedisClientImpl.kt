package com.hc.infra.redis.client

import com.hc.infra.redis.model.RedisKey
import com.hc.infra.redis.policy.RedisException
import com.hc.infra.redis.policy.RedisPolicy
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

class RedisClientImpl(
    private val redisTemplate: RedisTemplate<String, Any>
): RedisClient {

    override fun raw(key: RedisKey): Any? =
        redisTemplate.opsForValue().get(key.value)

    override fun set(key: RedisKey, value: Any) {
        redisTemplate.opsForValue().set(key.value, value)
    }

    override fun setWithTtl(key: RedisKey, value: Any, ttl: Duration) {
        validateTtlOrThrow(ttl)
        redisTemplate.opsForValue().set(key.value, value, ttl)
    }

    override fun setIfAbsent(key: RedisKey, value: Any): Boolean {
        return redisTemplate.opsForValue()
            .setIfAbsent(key.value, value)?: false
    }

    override fun setIfAbsentWithTtl(key: RedisKey, value: Any, ttl: Duration): Boolean {
        this.validateTtlOrThrow(ttl)
        return redisTemplate.opsForValue()
            .setIfAbsent(key.value, value, ttl)?: false
    }

    override fun delete(key: RedisKey): Boolean =
        redisTemplate.delete(key.value)?: false

    override fun exists(key: RedisKey): Boolean =
        redisTemplate.hasKey(key.value)?: false

    override fun expire(key: RedisKey, ttl: Duration): Boolean {
        validateTtlOrThrow(ttl)
        return redisTemplate.expire(key.value, ttl)?: false
    }

    override fun <T : Any> executeScript(
        script: DefaultRedisScript<T>,
        keys: List<RedisKey>,
        vararg args: Any
    ): T? {
        return redisTemplate.execute(script, keys.map { it.value }, *args)
    }

    override fun validateTtlOrThrow(ttl: Duration) {
        if (ttl.isZero || ttl.isNegative) {
            throw RedisException(
                policy = RedisPolicy.TTL_MUST_BE_POSITIVE,
                attributes = mapOf(
                    "ttl" to ttl
                )
            )
        }
    }

}