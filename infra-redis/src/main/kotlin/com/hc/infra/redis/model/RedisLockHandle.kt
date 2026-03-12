package com.hc.infra.redis.model

data class RedisLockHandle(
    val key: RedisKey,
    val owner: String,
    val fencingToken: Long
)
