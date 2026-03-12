package com.hc.infra.redis.client

import com.hc.infra.redis.model.RedisKey
import com.hc.infra.redis.policy.RedisException
import com.hc.infra.redis.policy.RedisPolicy

inline fun <reified T> RedisClient.get(key: RedisKey): T? {
    val value = raw(key) ?: return null

    if (value !is T) {
        throw RedisException(
            policy = RedisPolicy.CLASS_CAST_FAILED,
            attributes = mapOf(
                "expectedType" to (T::class.simpleName ?: "unknown"),
                "actualType" to (value::class.simpleName ?: "unknown")
            )
        )
    }

    return value
}

inline fun <reified T> RedisClient.getOrThrow(key: RedisKey): T {
    val value = raw(key)
        ?: throw RedisException(
            policy = RedisPolicy.REQUIRED_KEY_NOT_FOUND,
            attributes = mapOf("key" to key.value)
        )

    if (value !is T) {
        throw RedisException(
            policy = RedisPolicy.CLASS_CAST_FAILED,
            attributes = mapOf(
                "expectedType" to (T::class.simpleName ?: "unknown"),
                "actualType" to (value::class.simpleName ?: "unknown")
            )
        )
    }

    return value
}