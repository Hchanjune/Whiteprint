package com.hc.infra.redis.operation

import com.hc.core.cache.client.CacheClient
import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.operation.AtomicOperations
import com.hc.core.cache.policy.CacheException
import com.hc.core.cache.policy.CachePolicy
import com.hc.infra.redis.model.LuaScript
import java.time.Duration

class RedisAtomicOperations(
    private val client: CacheClient,
) : AtomicOperations {

    companion object {
        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: ttlMillis
         */
        private const val INCREMENT_AND_EXPIRE_SCRIPT = """
            local current = redis.call('INCRBY', KEYS[1], ARGV[1])
            if current == tonumber(ARGV[1]) then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            return current
        """

        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: limit
         */
        private const val INCREMENT_WITH_LIMIT_SCRIPT = """
            local current = tonumber(redis.call('GET', KEYS[1]) or "0")
            local delta = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            if current + delta > limit then return {0, current} end
            return {1, redis.call('INCRBY', KEYS[1], delta)}
        """

        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: limit
         * - ARGV[3]: ttlMillis
         */
        private const val INCREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT = """
            local current = tonumber(redis.call('GET', KEYS[1]) or "0")
            local delta = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            if current + delta > limit then return {0, current} end
            local next_val = redis.call('INCRBY', KEYS[1], delta)
            if next_val == delta then
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
            end
            return {1, next_val}
        """

        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: ttlMillis
         */
        private const val DECREMENT_AND_EXPIRE_SCRIPT = """
            local current = redis.call('DECRBY', KEYS[1], ARGV[1])
            if current == -tonumber(ARGV[1]) then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            return current
        """

        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: limit (min)
         */
        private const val DECREMENT_WITH_LIMIT_SCRIPT = """
            local current = tonumber(redis.call('GET', KEYS[1]) or "0")
            local delta = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            if current - delta < limit then return {0, current} end
            return {1, redis.call('DECRBY', KEYS[1], delta)}
        """

        /**
         * - KEYS[1]: key
         * - ARGV[1]: delta
         * - ARGV[2]: limit (min)
         * - ARGV[3]: ttlMillis
         */
        private const val DECREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT = """
            local current = tonumber(redis.call('GET', KEYS[1]) or "0")
            local delta = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            if current - delta < limit then return {0, current} end
            local next_val = redis.call('DECRBY', KEYS[1], delta)
            if next_val == -delta then
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
            end
            return {1, next_val}
        """

        private val incrementAndExpireScript = LuaScript(
            script = INCREMENT_AND_EXPIRE_SCRIPT,
            resultType = Long::class.java
        )

        private val decrementAndExpireScript = LuaScript(
            script = DECREMENT_AND_EXPIRE_SCRIPT,
            resultType = Long::class.java
        )

        private val incrementWithLimitScript = LuaScript(
            script = INCREMENT_WITH_LIMIT_SCRIPT,
            resultType = List::class.java
        )
        private val incrementWithLimitAndExpireScript = LuaScript(
            script = INCREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT,
            resultType = List::class.java
        )

        private val decrementWithLimitScript = LuaScript(
            script = DECREMENT_WITH_LIMIT_SCRIPT,
            resultType = List::class.java
        )
        private val decrementWithLimitAndExpireScript = LuaScript(
            script = DECREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT,
            resultType = List::class.java
        )
    }

    override fun incrementOrThrow(key: CacheKey, delta: Long): Long {
        return client.incrementOrThrow(key, delta)
    }

    override fun incrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long): Long {
        client.validateTtlOrThrow(ttl)
        return client.executeScript(
            script = incrementAndExpireScript,
            keys = listOf(key),
            delta,
            ttl.toMillis()
        ) ?:
            throw CacheException(
                policy = CachePolicy.INCREMENT_FAILED,
                attributes = mapOf(
                    "key" to key.value,
                    "delta" to delta
                )
            )
    }

    override fun incrementWithLimitOrThrow(key: CacheKey, delta: Long, limit: Long): Long {
        val result = client.executeScript(
            script = incrementWithLimitScript,
            keys = listOf(key),
            delta,
            limit
        )
        return handleLimitResult(result, key, delta, limit, isIncrement = true)
    }

    override fun incrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long, limit: Long, ttl: Duration): Long {
        client.validateTtlOrThrow(ttl)
        val result = client.executeScript(
            script = incrementWithLimitAndExpireScript,
            keys = listOf(key),
            delta,
            limit,
            ttl.toMillis()
        )
        return handleLimitResult(result, key, delta, limit, isIncrement = true)
    }

    override fun decrementOrThrow(key: CacheKey, delta: Long): Long {
        return client.decrementOrThrow(key, delta)
    }

    override fun decrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long): Long {
        client.validateTtlOrThrow(ttl)
        return client.executeScript(
            script = decrementAndExpireScript,
            keys = listOf(key),
            delta,
            ttl.toMillis()
        ) ?:
        throw CacheException(
            policy = CachePolicy.DECREMENT_FAILED,
            attributes = mapOf(
                "key" to key.value,
                "delta" to delta
            )
        )
    }

    override fun decrementWithLimitOrThrow(key: CacheKey, delta: Long, limit: Long): Long {
        val result = client.executeScript(
            script = decrementWithLimitScript,
            keys = listOf(key),
            delta,
            limit
        )
        return handleLimitResult(result, key, delta, limit, isIncrement = false)
    }

    override fun decrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long, limit: Long, ttl: Duration): Long {
        client.validateTtlOrThrow(ttl)
        val result = client.executeScript(
            script = decrementWithLimitAndExpireScript,
            keys = listOf(key),
            delta,
            limit,
            ttl.toMillis()
        )
        return handleLimitResult(result, key, delta, limit, isIncrement = false)
    }

    private fun handleLimitResult(
        result: List<*>?,
        key: CacheKey,
        delta: Long,
        limit: Long,
        isIncrement: Boolean
    ): Long {
        if (result == null || result.size < 2) {
            throw CacheException(
                policy = CachePolicy.INFRA_FAILURE,
                attributes = mapOf(
                    "key" to key.value,
                    "reason" to "Invalid Lua script response format"
                )
            )
        }

        val status = (result[0] as? Number)?.toLong() ?: 0L
        val value = (result[1] as? Number)?.toLong() ?: 0L

        if (status == 1L) return value

        val policy = if (isIncrement) CachePolicy.INCREMENT_LIMIT_EXCEEDED
        else CachePolicy.DECREMENT_LIMIT_EXCEEDED

        throw CacheException(
            policy = policy,
            attributes = mapOf(
                "key" to key.value,
                "current" to value,
                "delta" to delta,
                "limit" to limit
            )
        )
    }
}