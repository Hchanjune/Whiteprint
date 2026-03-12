package com.hc.infra.redis.operation

import com.hc.infra.redis.client.RedisClient
import java.time.Duration

class RedisLockOperations(
    private val client: RedisClient,
    private val scriptOps: RedisScriptOperations
) {

    fun acquireLock(key:String, owner: String, ttl: Duration): Boolean =
        client.setIfAbsent(key, owner, ttl)

    fun releaseLock(key: String ,owner: String): Boolean =
        scriptOps.releaseLock(key, owner)

}