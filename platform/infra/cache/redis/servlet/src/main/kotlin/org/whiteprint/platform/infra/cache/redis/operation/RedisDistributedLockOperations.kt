package org.whiteprint.platform.infra.cache.redis.operation

import org.whiteprint.platform.infra.cache.redis.model.FencingKey
import org.whiteprint.platform.infra.cache.redis.model.LuaScript
import org.springframework.data.redis.core.RedisTemplate
import org.whiteprint.platform.core.cache.model.CacheValidator
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import java.time.Duration
import java.util.UUID

/**
 * Redis `SET NX PX` 기반 분산 락.
 *
 * ## owner 는 **획득마다** 새로 만든다
 * 락 값(owner)은 `인스턴스 식별자:획득 UUID` 다. 해제·연장 스크립트는 "저장된 값 == 내 owner" 일 때만 동작하므로,
 * owner 가 인스턴스 단위로 같으면 **같은 인스턴스의 다른 스레드가 잡은 락을 풀어버린다** —
 * 내 락이 TTL 로 만료된 사이 같은 인스턴스의 다른 요청이 새로 잡았는데, 늦게 끝난 내가 그 락을 지우는 경우다.
 * 인스턴스 식별자는 Redis 에서 누가 쥐고 있는지 볼 때 쓰는 진단용이다.
 *
 * ## 펜싱 토큰은 **전역 카운터 하나**에서 발급한다
 * - 락 키마다 카운터를 두면 락 키(주문 id 등) 수만큼 TTL 없는 카운터 키가 영구히 쌓인다.
 * - 키마다 따로 세면 서로 다른 키의 토큰끼리 크기를 비교할 수 없다. 전역이면 모든 토큰이 발급 순서대로 단조 증가한다.
 *
 * ## 스크립트 인자는 전부 문자열로 넘긴다
 * 락 템플릿의 값 직렬화기가 `StringRedisSerializer` 라 스크립트 인자도 그걸로 직렬화된다 — `Long` 을 넘기면
 * `ClassCastException` 으로 **모든 획득이 실패**한다(0.8.2 까지의 버그, 실제 Redis 실측으로 발견). Lua 쪽 `PX` 는 문자열 숫자를 받는다.
 *
 * ⚠ 획득 스크립트가 락 키와 카운터 키를 함께 쓰므로 Redis Cluster 에서는 같은 슬롯이 아니면 실패한다. 지금은 standalone 전제다.
 */
class RedisDistributedLockOperations(
    private val redisTemplate: RedisTemplate<String, Any>,
    /** 인스턴스 식별자. owner 의 앞부분이 된다. */
    private val instanceOwner: String,
): DistributedLockOperations {

    companion object {

        /** 모든 락이 공유하는 펜싱 토큰 카운터. */
        private val GLOBAL_FENCING_KEY = FencingKey("whiteprint:lock:fencing-token")

        /**
         * - KEYS[1]: lockKey
         * - KEYS[2]: 전역 펜싱 카운터
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

        private val acquireLockWithTokenScript =
            LuaScript(
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
        key: LockKey,
        ttl: Duration
    ): LockHandle? {

        CacheValidator.validateTtlOrThrow(ttl)

        val owner = "$instanceOwner:${UUID.randomUUID()}"

        val token = redisTemplate.execute(
            acquireLockWithTokenScript.redisScript,
            listOf(key.value, GLOBAL_FENCING_KEY.value),
            owner,
            ttl.toMillis().toString(),
        )

        if (token == null || token <= 0) return null

        return LockHandle(
            key = key,
            owner = owner,
            fencingToken = token
        )
    }

    /** @return 내가 쥔 락을 지웠으면 true. 이미 만료됐거나 다른 owner 가 쥐고 있으면 false. */
    override fun releaseLock(lock: LockHandle): Boolean {

        val result = redisTemplate.execute(
            releaseLockScript.redisScript,
            listOf(lock.key.value),
            lock.owner,
        )

        return result == 1L
    }

    override fun extendLock(lock: LockHandle, ttl: Duration): Boolean {

        CacheValidator.validateTtlOrThrow(ttl)

        val result = redisTemplate.execute(
            extendLockScript.redisScript,
            listOf(lock.key.value),
            lock.owner,
            ttl.toMillis().toString(),
        )

        return result == 1L
    }

}
