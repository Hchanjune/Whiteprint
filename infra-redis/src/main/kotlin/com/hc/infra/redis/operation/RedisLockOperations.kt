package com.hc.infra.redis.operation

import com.hc.infra.redis.client.RedisClientImpl
import com.hc.infra.redis.model.RedisKey
import com.hc.infra.redis.model.RedisLockHandle
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

class RedisLockOperations(
    private val client: RedisClientImpl,
    private val owner: String
) {

    companion object {
        private const val ACQUIRE_LOCK_SCRIPT = """
            if redis.call("set", KEYS[1], ARGV[1], "NX", "PX", ARGV[2]) then
                return redis.call("incr", KEYS[2])
            else
                return 0
            end
        """

        private const val RELEASE_LOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1]
            then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """

        private const val EXTEND_LOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1]
            then
                return redis.call("pexpire", KEYS[1], ARGV[2])
            else
                return 0
            end
        """

        private val acquireLockWithTokenScript = DefaultRedisScript<Long>().apply {
            setScriptText(ACQUIRE_LOCK_SCRIPT)
            resultType = Long::class.java
        }

        private val releaseLockScript = DefaultRedisScript<Long>().apply {
            setScriptText(RELEASE_LOCK_SCRIPT)
            resultType = Long::class.java
        }

        private val extendLockScript = DefaultRedisScript<Long>().apply {
            setScriptText(EXTEND_LOCK_SCRIPT)
            resultType = Long::class.java
        }

    }



    fun acquireLock(
        key: RedisKey,
        ttl: Duration
    ): RedisLockHandle? {

        client.validateTtlOrThrow(ttl)

        val fencingKey = RedisKey("$key:fencing")

        val token = client.executeScript(
            script = acquireLockWithTokenScript,
            keys = listOf(key, fencingKey),
            owner,
            ttl.toMillis()
        )

        if (token == null || token <= 0) return null

        return RedisLockHandle(
            key = key,
            owner = owner,
            fencingToken = token
        )
    }

    fun releaseLock(lock: RedisLockHandle): Boolean {

        val result = client.executeScript(
            script = releaseLockScript,
            keys = listOf(lock.key),
            lock.owner
        )

        return result == 1L
    }

    fun extendLock(lock: RedisLockHandle, ttl: Duration): Boolean {

        client.validateTtlOrThrow(ttl)

        val result = client.executeScript(
            script = extendLockScript,
            keys = listOf(lock.key),
            lock.owner,
            ttl.toMillis()
        )

        return result == 1L
    }

}