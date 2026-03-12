package com.hc.infra.redis.client

import com.hc.infra.redis.policy.RedisException
import com.hc.infra.redis.policy.RedisPolicy
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class RedisClient(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun raw(key: String): Any? =
        redisTemplate.opsForValue().get(key)

    inline fun <reified T> get(key: String): T? = this.raw(key) as? T

    inline fun <reified T> require(key: String): T = this.raw(key) as? T
        ?: throw RedisException(
            policy = RedisPolicy.REQUIRED_KEY_NOT_FOUND,
            attributes = mapOf(
                "key" to key,
            )
        )

    fun set(key: String, value: Any, ttl: Duration? = null) {
        if (ttl != null) {
            redisTemplate.opsForValue().set(key, value, ttl)
        } else {
            redisTemplate.opsForValue().set(key, value)
        }
    }

    fun setIfAbsent(key: String, value: Any, ttl: Duration): Boolean {
        return redisTemplate.opsForValue()
            .setIfAbsent(key, value, ttl) ?: false
    }

}