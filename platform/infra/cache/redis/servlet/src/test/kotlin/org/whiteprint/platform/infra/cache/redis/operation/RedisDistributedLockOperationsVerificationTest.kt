package org.whiteprint.platform.infra.cache.redis.operation

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.whiteprint.platform.core.lock.model.LockKey
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * 락 Lua 스크립트를 **실제 Redis** 로 실측한다 — owner 가 획득마다 달라 해제가 남의 락을 지우지 않는지,
 * 펜싱 토큰이 전역 카운터 하나에서 나오는지는 스크립트와 Redis 가 함께 돌아야 확정된다.
 *
 * 전용 DB 번호(기본 15)의 `whiteprint-test:lock:<실행 UUID>:*` 키만 쓰고 끝나면 지운다. 접속 실패 시 skip.
 * 접속 override: LOCK_REDIS_HOST / LOCK_REDIS_PORT / LOCK_REDIS_PASSWORD / LOCK_REDIS_DATABASE
 */
@EnabledIf("redisAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisDistributedLockOperationsVerificationTest {

    companion object {
        private val host = System.getenv("LOCK_REDIS_HOST") ?: "192.168.0.12"
        private val port = (System.getenv("LOCK_REDIS_PORT") ?: "6379").toInt()
        private val password = System.getenv("LOCK_REDIS_PASSWORD")
        private val database = (System.getenv("LOCK_REDIS_DATABASE") ?: "15").toInt()

        private const val INSTANCE = "test-host:abcd1234"
        private const val GLOBAL_FENCING_KEY = "whiteprint:lock:fencing-token"

        private val factory: LettuceConnectionFactory by lazy {
            LettuceConnectionFactory(
                RedisStandaloneConfiguration(host, port).apply {
                    this.database = Companion.database
                    password?.let { setPassword(it) }
                },
                LettuceClientConfiguration.builder().commandTimeout(Duration.ofSeconds(2)).build(),
            ).apply { afterPropertiesSet() }
        }

        private val template: RedisTemplate<String, Any> by lazy {
            RedisTemplate<String, Any>().apply {
                connectionFactory = factory
                keySerializer = RedisSerializer.string()
                valueSerializer = RedisSerializer.string()
                afterPropertiesSet()
            }
        }

        @JvmStatic
        fun redisAvailable(): Boolean = try {
            factory.connection.use { it.ping() } != null
        } catch (e: Exception) {
            false
        }
    }

    private val run = UUID.randomUUID().toString().take(8)
    private val operations by lazy { RedisDistributedLockOperations(template, INSTANCE) }

    private fun key(name: String) = LockKey("whiteprint-test:lock:$run:$name")

    private fun redisValue(key: LockKey): String? = template.opsForValue().get(key.value) as String?

    @AfterEach
    fun cleanUp() {
        template.keys("whiteprint-test:lock:$run:*").takeIf { it.isNotEmpty() }?.let { template.delete(it) }
    }

    @AfterAll
    fun close() {
        factory.destroy()
    }

    @Test
    @DisplayName("owner 는 획득마다 다르고, Redis 에 그 owner 가 저장된다")
    fun ownerIsPerAcquisition() {
        val first = operations.acquireLock(key("a"), Duration.ofSeconds(5))!!
        assertTrue(operations.releaseLock(first))
        val second = operations.acquireLock(key("a"), Duration.ofSeconds(5))!!

        assertTrue(first.owner.startsWith("$INSTANCE:"))
        assertTrue(second.owner.startsWith("$INSTANCE:"))
        assertNotEquals(first.owner, second.owner)
        assertEquals(second.owner, redisValue(key("a")))
    }

    @Test
    @DisplayName("쥐고 있는 동안에는 같은 인스턴스에서도 다시 못 잡는다")
    fun heldLockCannotBeAcquiredAgain() {
        assertNotNull(operations.acquireLock(key("b"), Duration.ofSeconds(5)))
        assertNull(operations.acquireLock(key("b"), Duration.ofSeconds(5)))
    }

    @Test
    @DisplayName("핵심 버그 재현: 만료 후 같은 인스턴스의 다른 요청이 잡은 락을, 늦게 끝난 옛 보유자가 지우지 못한다")
    fun staleHolderCannotReleaseNewHoldersLock() {
        val stale = operations.acquireLock(key("c"), Duration.ofMillis(200))!!
        Thread.sleep(350)
        val fresh = operations.acquireLock(key("c"), Duration.ofSeconds(5))!!

        assertFalse(operations.releaseLock(stale), "stale holder must not delete the new holder's lock")
        assertEquals(fresh.owner, redisValue(key("c")))
        assertTrue(operations.releaseLock(fresh))
        assertNull(redisValue(key("c")))
    }

    @Test
    @DisplayName("연장은 내 락만: 내 락은 TTL 이 늘고, 옛 핸들로는 새 보유자의 TTL 을 못 건드린다")
    fun extendOnlyOwnLock() {
        val holder = operations.acquireLock(key("d"), Duration.ofMillis(500))!!
        assertTrue(operations.extendLock(holder, Duration.ofSeconds(10)))
        assertTrue(template.getExpire(key("d").value, TimeUnit.MILLISECONDS) > 5_000)

        assertTrue(operations.releaseLock(holder))
        val next = operations.acquireLock(key("d"), Duration.ofSeconds(3))!!
        assertFalse(operations.extendLock(holder, Duration.ofSeconds(60)))
        assertTrue(template.getExpire(key("d").value, TimeUnit.MILLISECONDS) <= 3_000)
        operations.releaseLock(next)
    }

    @Test
    @DisplayName("펜싱 토큰은 키가 달라도 하나의 전역 카운터에서 단조 증가하고, 키별 카운터 키는 생기지 않는다")
    fun fencingTokenIsGlobalAndMonotonic() {
        val tokens = listOf("e1", "e2", "e1", "e3").map { name ->
            operations.acquireLock(key(name), Duration.ofSeconds(5))!!.also { operations.releaseLock(it) }.fencingToken
        }

        assertEquals(tokens.sorted(), tokens)
        assertEquals(tokens.size, tokens.toSet().size)
        assertTrue((template.opsForValue().get(GLOBAL_FENCING_KEY) as String).toLong() >= tokens.last())
        assertTrue(template.keys("whiteprint-test:lock:$run:*:fencing").isEmpty())
    }

    @Test
    @DisplayName("아무도 없는 만료된 락의 해제는 false")
    fun releaseAfterExpiryReturnsFalse() {
        val lock = operations.acquireLock(key("f"), Duration.ofMillis(150))!!
        Thread.sleep(300)
        assertFalse(operations.releaseLock(lock))
    }

}
