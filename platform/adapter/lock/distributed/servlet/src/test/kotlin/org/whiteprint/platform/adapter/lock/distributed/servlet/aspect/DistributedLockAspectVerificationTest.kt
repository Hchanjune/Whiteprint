package org.whiteprint.platform.adapter.lock.distributed.servlet.aspect

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.whiteprint.platform.adapter.lock.distributed.servlet.watchdog.DistributedLockWatchdog
import org.whiteprint.platform.core.lock.annotation.DistributedLock
import org.whiteprint.platform.core.lock.annotation.DistributedLockKey
import org.whiteprint.platform.core.lock.context.LockContext
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import org.whiteprint.platform.core.lock.policy.LockException
import org.whiteprint.platform.core.lock.policy.LockPolicy
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 애스펙트 동작을 실제 프록시로 실측한다. Redis 대신 메모리 가짜 락 연산을 쓴다 — 여기서 보는 건
 * 재진입·컨텍스트 정리·해제 실패 처리·자동연장 배선이지 Redis 스크립트가 아니다.
 */
class DistributedLockAspectVerificationTest {

    class FakeLockOperations : DistributedLockOperations {
        val owners = ConcurrentHashMap<String, String>()
        val acquires = AtomicInteger()
        val releases = AtomicInteger()
        val extends = AtomicInteger()
        private val tokens = AtomicLong()

        @Volatile var releaseThrows = false
        @Volatile var releaseLost = false

        override fun acquireLock(key: LockKey, ttl: Duration): LockHandle? {
            acquires.incrementAndGet()
            val owner = "owner-${UUID.randomUUID()}"
            return if (owners.putIfAbsent(key.value, owner) == null) LockHandle(key, owner, tokens.incrementAndGet()) else null
        }

        override fun releaseLock(lock: LockHandle): Boolean {
            releases.incrementAndGet()
            if (releaseThrows) throw IllegalStateException("redis down")
            val removed = owners.remove(lock.key.value, lock.owner)
            return removed && !releaseLost
        }

        override fun extendLock(lock: LockHandle, ttl: Duration): Boolean {
            extends.incrementAndGet()
            return owners[lock.key.value] == lock.owner
        }
    }

    open class InnerService {
        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 200)
        open fun tokenInside(@DistributedLockKey id: Long): Long? = LockContext.currentFencingToken()
    }

    open class OuterService(private val inner: InnerService) {

        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 200)
        open fun sameKey(@DistributedLockKey id: Long): Pair<Long?, Long?> =
            LockContext.currentFencingToken() to inner.tokenInside(id)

        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 200)
        open fun otherKey(@DistributedLockKey id: Long): Pair<Long?, Long?> =
            LockContext.currentFencingToken() to inner.tokenInside(id + 1)

        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 200)
        open fun returns(@DistributedLockKey id: Long): String = "result"

        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 200)
        open fun throws(@DistributedLockKey id: Long): String = throw IllegalArgumentException("business failure")

        @DistributedLock(prefix = "test:order", ttl = 150, wait = 100)
        open fun slow(@DistributedLockKey id: Long): String {
            Thread.sleep(500)
            return "slow"
        }
    }

    private val operations = FakeLockOperations()
    private val watchdog = DistributedLockWatchdog(operations)
    private val aspect = DistributedLockAspect(operations, { UUID.randomUUID().toString() }, watchdog)

    private fun <T : Any> proxy(target: T): T =
        AspectJProxyFactory(target).apply {
            isProxyTargetClass = true
            addAspect(aspect)
        }.getProxy()

    private val inner = proxy(InnerService())
    private val outer = proxy(OuterService(inner))

    @AfterEach
    fun tearDown() {
        watchdog.destroy()
    }

    @Test
    @DisplayName("재진입: 같은 스레드가 같은 키를 다시 잡으면 획득 없이 통과하고, 바깥 락 하나만 획득·해제된다")
    fun reentrantSameKey() {
        val (outerToken, innerToken) = outer.sameKey(1)

        assertNotNull(outerToken)
        assertEquals(outerToken, innerToken)
        assertEquals(1, operations.acquires.get())
        assertEquals(1, operations.releases.get())
        assertTrue(operations.owners.isEmpty())
    }

    @Test
    @DisplayName("다른 키는 재진입이 아니다: 둘 다 획득·해제되고, 안쪽 토큰 판단은 가장 오래된(바깥) 토큰이다")
    fun nestedOtherKey() {
        val (outerToken, innerToken) = outer.otherKey(1)

        assertEquals(2, operations.acquires.get())
        assertEquals(2, operations.releases.get())
        assertEquals(outerToken, innerToken)
        assertTrue(operations.owners.isEmpty())
    }

    @Test
    @DisplayName("끝나면 컨텍스트가 비고, 예외로 끝나도 비고 락도 풀린다")
    fun contextIsClearedEvenOnException() {
        outer.returns(1)
        assertNull(LockContext.currentFencingToken())

        assertThrows(IllegalArgumentException::class.java) { outer.throws(2) }
        assertNull(LockContext.currentFencingToken())
        assertTrue(operations.owners.isEmpty())
    }

    @Test
    @DisplayName("해제가 예외를 던져도 반환값·원래 예외를 덮지 않는다")
    fun releaseFailureDoesNotMaskOutcome() {
        operations.releaseThrows = true

        assertEquals("result", outer.returns(1))
        val error = assertThrows(IllegalArgumentException::class.java) { outer.throws(2) }
        assertEquals("business failure", error.message)
    }

    @Test
    @DisplayName("해제 시 이미 락을 잃었어도(false) 예외 없이 끝난다")
    fun lostLockAtReleaseIsOnlyWarned() {
        operations.releaseLost = true
        assertEquals("result", outer.returns(1))
    }

    @Test
    @DisplayName("자동연장: ttl(150ms)보다 긴 실행(500ms) 동안 연장되고, 끝난 뒤 정상 해제된다")
    fun autoExtendKeepsLockDuringLongExecution() {
        assertEquals("slow", outer.slow(1))

        assertTrue(operations.extends.get() >= 2, "extends=${operations.extends.get()}")
        assertEquals(1, operations.releases.get())
        assertTrue(operations.owners.isEmpty())

        val extendsAfter = operations.extends.get()
        Thread.sleep(200)
        assertEquals(extendsAfter, operations.extends.get(), "extension must stop after the method ends")
    }

    @Test
    @DisplayName("다른 요청이 쥐고 있으면 wait 뒤 LOCK_ACQUISITION_FAILED")
    fun busyLockFailsAfterWait() {
        operations.owners["test:order:9"] = "someone-else"

        val error = assertThrows(LockException::class.java) { outer.returns(9) }
        assertEquals(LockPolicy.ACQUISITION_FAILED.code, error.code)
        assertNull(LockContext.currentFencingToken())
    }

}
