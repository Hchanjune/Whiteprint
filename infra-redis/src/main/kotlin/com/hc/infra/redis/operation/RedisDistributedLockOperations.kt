package com.hc.infra.redis.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheLockHandle
import com.hc.core.cache.operation.DistributedLockOperations
import com.hc.infra.redis.client.RedisClient
import com.hc.infra.redis.client.executeScript
import com.hc.infra.redis.model.FencingKey
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

class RedisDistributedLockOperations(
    private val client: RedisClient,
    private val owner: String
): DistributedLockOperations {

    companion object {

        private const val FENCING_TOKEN_SUFFIX = ":fencing"

        /**
         * - ARGV[1]: owner
         * - ARGV[2]: ttlMillis
         */
        private const val ACQUIRE_LOCK_SCRIPT = """
            if redis.call("set", KEYS[1], ARGV[1], "NX", "PX", ARGV[2]) then
                return redis.call("incr", KEYS[2])
            else
                return 0
            end
        """

        /**
         * - KEYS[1]: lockKey
         * - ARGV[1]: owner
         */
        private const val RELEASE_LOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1]
            then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """

        /**
         * - KEYS[1]: lockKey
         * - ARGV[1]: owner
         * - ARGV[2]: ttlMillis
         */
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

    override fun acquireLock(
        key: CacheKey,
        ttl: Duration
    ): CacheLockHandle? {

        client.validateTtlOrThrow(ttl)

        val fencingKey = FencingKey("${key.value}$FENCING_TOKEN_SUFFIX")

        val token = client.executeScript(
            script = acquireLockWithTokenScript,
            keys = listOf(key, fencingKey),
            owner,
            ttl.toMillis()
        )

        if (token == null || token <= 0) return null

        return CacheLockHandle(
            key = key,
            owner = owner,
            fencingToken = token
        )
    }

    override fun releaseLock(lock: CacheLockHandle): Boolean {

        val result = client.executeScript(
            script = releaseLockScript,
            keys = listOf(lock.key),
            lock.owner
        )

        return result == 1L
    }

    override fun extendLock(lock: CacheLockHandle, ttl: Duration): Boolean {

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