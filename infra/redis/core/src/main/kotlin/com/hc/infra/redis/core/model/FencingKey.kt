package com.hc.infra.redis.core.model

import com.hc.core.cache.model.CacheKey

@JvmInline
value class FencingKey(
    override val value: String
): CacheKey