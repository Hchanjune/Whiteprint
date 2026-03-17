package com.hc.infra.redis.core.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheLockHandle
import com.hc.core.cache.model.CacheValidator
import com.hc.core.cache.operation.DistributedLockOperations
import com.hc.infra.redis.core.model.FencingKey
import com.hc.infra.redis.core.model.LuaScript
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class RedisDistributedLockOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
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

        private val acquireLockWithTokenScript = LuaScript(
            script = ACQUIRE_LOCK_SCRIPT,
            resultType = Long::class.java,
        )
        private val releaseLockScript = LuaScript(
            script = RELEASE_LOCK_SCRIPT,
            resultType = Long::class.java,
        )

        private val extendLockScript = LuaScript(
            script = EXTEND_LOCK_SCRIPT,
            resultType = Long::class.java,
        )

    }

    override fun acquireLock(
        key: CacheKey,
        ttl: Duration
    ): CacheLockHandle? {

        CacheValidator.validateTtlOrThrow(ttl)

        val fencingKey = FencingKey("${key.value}$FENCING_TOKEN_SUFFIX")

        val token = redisTemplate.execute(
            acquireLockWithTokenScript.redisScript,
            listOf(key.value, fencingKey.value),
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

        val result = redisTemplate.execute(
            releaseLockScript.redisScript,
            listOf(lock.key.value),
            lock.owner,
        )

        return result == 1L
    }

    override fun extendLock(lock: CacheLockHandle, ttl: Duration): Boolean {

        CacheValidator.validateTtlOrThrow(ttl)

        val result = redisTemplate.execute(
            extendLockScript.redisScript,
            listOf(lock.key.value),
            lock.owner,
            ttl.toMillis()
        )

        return result == 1L
    }

}