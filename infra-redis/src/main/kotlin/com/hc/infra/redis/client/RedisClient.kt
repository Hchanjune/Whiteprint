package com.hc.infra.redis.client

import com.hc.core.cache.client.CacheClient
import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheScript
import com.hc.core.cache.policy.CacheException
import com.hc.core.cache.policy.CachePolicy
import com.hc.infra.redis.model.LuaScript
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.set

class RedisClient(
    private val redisTemplate: RedisTemplate<String, Any>
): CacheClient {

    override fun raw(key: CacheKey): Any? =
        redisTemplate.opsForValue().get(key.value)

    override fun set(key: CacheKey, value: Any) {
        redisTemplate.opsForValue().set(key.value, value)
    }

    override fun setWithTtl(key: CacheKey, value: Any, ttl: Duration) {
        validateTtlOrThrow(ttl)
        redisTemplate.opsForValue().set(key.value, value, ttl)
    }

    override fun setIfAbsent(key: CacheKey, value: Any): Boolean {
        return redisTemplate.opsForValue()
            .setIfAbsent(key.value, value)?: false
    }

    override fun setIfAbsentWithTtl(key: CacheKey, value: Any, ttl: Duration): Boolean {
        this.validateTtlOrThrow(ttl)
        return redisTemplate.opsForValue()
            .setIfAbsent(key.value, value, ttl)?: false
    }

    override fun delete(key: CacheKey): Boolean =
        redisTemplate.delete(key.value)?: false

    override fun exists(key: CacheKey): Boolean =
        redisTemplate.hasKey(key.value)?: false

    override fun expire(key: CacheKey, ttl: Duration): Boolean {
        validateTtlOrThrow(ttl)
        return redisTemplate.expire(key.value, ttl)?: false
    }

    override fun validateTtlOrThrow(ttl: Duration) {
        if (ttl.isZero || ttl.isNegative) {
            throw CacheException(
                policy = CachePolicy.TTL_MUST_BE_POSITIVE,
                attributes = mapOf(
                    "ttl" to ttl
                )
            )
        }
    }

    override fun <T : Any> executeScript(
        script: CacheScript<T>,
        keys: List<CacheKey>,
        vararg args: Any
    ): T? {
        val targetScript = when (script) {
            is LuaScript<T> -> script.redisScript
            else -> DefaultRedisScript(script.script, script.resultType)
        }
        return redisTemplate.execute(
            targetScript,
            keys.map { it.value },
            *args
        )
    }

    override fun incrementOrThrow(key: CacheKey, delta: Long): Long {
        return redisTemplate.opsForValue().increment(key.value, delta)?:
            throw CacheException(
                policy = CachePolicy.INCREMENT_FAILED,
                attributes = mapOf(
                    "key" to key.value,
                    "delta" to delta
                )
            )
    }

    override fun decrementOrThrow(key: CacheKey, delta: Long): Long {
        return redisTemplate.opsForValue().increment(key.value, delta)?:
        throw CacheException(
            policy = CachePolicy.DECREMENT_FAILED,
            attributes = mapOf(
                "key" to key.value,
                "delta" to delta
            )
        )
    }

    override fun multiGetRaw(keys: List<CacheKey>): List<Any?> {
        if (keys.isEmpty()) return emptyList()
        return redisTemplate.opsForValue().multiGet(keys.map { it.value }) ?: keys.map { null }
    }

    override fun multiSet(map: Map<CacheKey, Any>) {
        if (map.isEmpty()) return
        redisTemplate.opsForValue().multiSet(map.mapKeys { it.key.value })
    }

    override fun multiDelete(keys: List<CacheKey>) {
        if (keys.isEmpty()) return
        redisTemplate.delete(keys.map { it.value })
    }

    override fun executePipelined(action: () -> Unit) {
        redisTemplate.executePipelined {
            action()
            null
        }
    }

}