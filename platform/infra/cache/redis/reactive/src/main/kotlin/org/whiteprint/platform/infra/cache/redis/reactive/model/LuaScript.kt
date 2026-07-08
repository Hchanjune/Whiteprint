package org.whiteprint.platform.infra.cache.redis.reactive.model

import org.springframework.data.redis.core.script.DefaultRedisScript
import org.whiteprint.platform.core.cache.model.CacheScript

class LuaScript<T : Any>(
    override val script: String,
    override val resultType: Class<T>,
) : CacheScript<T> {
    val redisScript: DefaultRedisScript<T> by lazy {
        DefaultRedisScript(script, resultType)
    }
}
