package com.hc.infra.redis.client

import com.hc.infra.redis.model.RedisKey
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

interface RedisClient {

    fun raw(key: RedisKey): Any?

    fun set(key: RedisKey, value: Any)

    fun setWithTtl(key: RedisKey, value: Any, ttl: Duration)

    fun setIfAbsent(key: RedisKey, value: Any): Boolean

    fun setIfAbsentWithTtl(key: RedisKey, value: Any, ttl: Duration): Boolean

    fun delete(key: RedisKey): Boolean

    fun exists(key: RedisKey): Boolean

    fun expire(key: RedisKey, ttl: Duration): Boolean

    fun <T : Any> executeScript(
        script: DefaultRedisScript<T>,
        keys: List<RedisKey>,
        vararg args: Any
    ): T?

    fun validateTtlOrThrow(ttl: Duration)
}