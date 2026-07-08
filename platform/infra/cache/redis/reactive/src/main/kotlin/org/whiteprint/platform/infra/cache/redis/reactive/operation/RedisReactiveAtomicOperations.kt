package org.whiteprint.platform.infra.cache.redis.reactive.operation

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CacheValidator
import org.whiteprint.platform.core.cache.operation.ReactiveAtomicOperations
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.infra.cache.redis.reactive.model.LuaScript
import java.time.Duration

class RedisReactiveAtomicOperations(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) : ReactiveAtomicOperations {

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

        private val incrementAndExpireScript = LuaScript(INCREMENT_AND_EXPIRE_SCRIPT, Long::class.java)
        private val decrementAndExpireScript = LuaScript(DECREMENT_AND_EXPIRE_SCRIPT, Long::class.java)
        private val incrementWithLimitScript = LuaScript(INCREMENT_WITH_LIMIT_SCRIPT, List::class.java)
        private val incrementWithLimitAndExpireScript = LuaScript(INCREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT, List::class.java)
        private val decrementWithLimitScript = LuaScript(DECREMENT_WITH_LIMIT_SCRIPT, List::class.java)
        private val decrementWithLimitAndExpireScript = LuaScript(DECREMENT_WITH_LIMIT_AND_EXPIRE_SCRIPT, List::class.java)
    }

    override suspend fun incrementOrThrow(key: CacheKey, delta: Long): Long =
        redisTemplate.opsForValue().increment(key.value, delta).awaitSingleOrNull()
            ?: throw CacheException(CachePolicy.INCREMENT_FAILED, mapOf("key" to key.value, "delta" to delta))

    override suspend fun incrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long): Long {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.execute(incrementAndExpireScript.redisScript, listOf(key.value), listOf(delta, ttl.toMillis()))
            .awaitFirstOrNull()
            ?: throw CacheException(CachePolicy.INCREMENT_FAILED, mapOf("key" to key.value, "delta" to delta))
    }

    override suspend fun incrementWithLimitOrThrow(key: CacheKey, delta: Long, limit: Long): Long {
        val result = redisTemplate.execute(incrementWithLimitScript.redisScript, listOf(key.value), listOf(delta, limit))
            .awaitFirstOrNull()
        return handleLimitResult(result, key, delta, limit, isIncrement = true)
    }

    override suspend fun incrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long, limit: Long, ttl: Duration): Long {
        CacheValidator.validateTtlOrThrow(ttl)
        val result = redisTemplate.execute(
            incrementWithLimitAndExpireScript.redisScript, listOf(key.value), listOf(delta, limit, ttl.toMillis())
        ).awaitFirstOrNull()
        return handleLimitResult(result, key, delta, limit, isIncrement = true)
    }

    override suspend fun decrementOrThrow(key: CacheKey, delta: Long): Long =
        redisTemplate.opsForValue().increment(key.value, -delta).awaitSingleOrNull()
            ?: throw CacheException(CachePolicy.DECREMENT_FAILED, mapOf("key" to key.value, "delta" to delta))

    override suspend fun decrementAndExpireOrThrow(key: CacheKey, ttl: Duration, delta: Long): Long {
        CacheValidator.validateTtlOrThrow(ttl)
        return redisTemplate.execute(decrementAndExpireScript.redisScript, listOf(key.value), listOf(delta, ttl.toMillis()))
            .awaitFirstOrNull()
            ?: throw CacheException(CachePolicy.DECREMENT_FAILED, mapOf("key" to key.value, "delta" to delta))
    }

    override suspend fun decrementWithLimitOrThrow(key: CacheKey, delta: Long, limit: Long): Long {
        val result = redisTemplate.execute(decrementWithLimitScript.redisScript, listOf(key.value), listOf(delta, limit))
            .awaitFirstOrNull()
        return handleLimitResult(result, key, delta, limit, isIncrement = false)
    }

    override suspend fun decrementWithLimitAndExpireOrThrow(key: CacheKey, delta: Long, limit: Long, ttl: Duration): Long {
        CacheValidator.validateTtlOrThrow(ttl)
        val result = redisTemplate.execute(
            decrementWithLimitAndExpireScript.redisScript, listOf(key.value), listOf(delta, limit, ttl.toMillis())
        ).awaitFirstOrNull()
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
                attributes = mapOf("key" to key.value, "reason" to "Invalid Lua script response format")
            )
        }

        val status = (result[0] as? Number)?.toLong() ?: 0L
        val value = (result[1] as? Number)?.toLong() ?: 0L

        if (status == 1L) return value

        val policy = if (isIncrement) CachePolicy.INCREMENT_LIMIT_EXCEEDED else CachePolicy.DECREMENT_LIMIT_EXCEEDED

        throw CacheException(
            policy = policy,
            attributes = mapOf("key" to key.value, "current" to value, "delta" to delta, "limit" to limit)
        )
    }
}
