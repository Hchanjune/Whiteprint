package org.whiteprint.platform.infra.cache.redis.model

import org.whiteprint.platform.core.cache.model.CacheKey

@JvmInline
value class FencingKey(
    override val value: String
): org.whiteprint.platform.core.cache.model.CacheKey