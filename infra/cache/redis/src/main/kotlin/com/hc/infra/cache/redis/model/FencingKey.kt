package com.hc.infra.cache.redis.model

import com.hc.core.cache.model.CacheKey

@JvmInline
value class FencingKey(
    override val value: String
): CacheKey