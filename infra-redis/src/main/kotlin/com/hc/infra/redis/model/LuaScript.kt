package com.hc.infra.redis.model

import com.hc.core.cache.model.CacheScript
import org.springframework.data.redis.core.script.DefaultRedisScript

class LuaScript<T : Any>(
    override val script: String,
    override val resultType: Class<T>,
) : CacheScript<T> {
    val redisScript: DefaultRedisScript<T> by lazy {
        DefaultRedisScript(script, resultType)
    }
}