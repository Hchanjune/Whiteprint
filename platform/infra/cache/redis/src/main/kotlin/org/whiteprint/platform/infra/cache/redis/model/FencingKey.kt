package org.whiteprint.platform.infra.cache.redis.model


@JvmInline
value class FencingKey(
    val value: String
)