package org.whiteprint.platform.adapter.lock.distributed.servlet.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.annotation.Order
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import org.whiteprint.platform.adapter.lock.distributed.servlet.watchdog.DistributedLockWatchdog
import org.whiteprint.platform.core.lock.annotation.DistributedLock
import org.whiteprint.platform.core.lock.annotation.DistributedLockKey
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * 실제 스프링 컨텍스트(`@EnableAspectJAutoProxy` + `@EnableTransactionManagement` 기본 설정)에서
 * 분산 락 애스펙트가 **어디에 끼는지** 실측한다. 순서는 애노테이션 값만 봐서는 확정되지 않는다 —
 * 트랜잭션 어드바이저와 애스펙트가 한 프록시 체인에서 어떻게 정렬되는지는 스프링이 정한다.
 *
 * 기대하는 바깥 → 안쪽: RateLimited(+11) → DistributedLock(+12) → Deduplicated(+13) → @Transactional(LOWEST) → 메서드
 */
class DistributedLockOrderVerificationTest {

    object Events {
        val list: MutableList<String> = Collections.synchronizedList(mutableListOf())
        fun add(event: String) {
            list += "$event(tx=${TransactionSynchronizationManager.isActualTransactionActive()})"
        }
    }

    class RecordingLockOperations : DistributedLockOperations {
        private val tokens = AtomicLong()
        override fun acquireLock(key: LockKey, ttl: Duration): LockHandle {
            Events.add("lock.acquire")
            return LockHandle(key, "owner-${UUID.randomUUID()}", tokens.incrementAndGet())
        }
        override fun releaseLock(lock: LockHandle): Boolean {
            Events.add("lock.release")
            return true
        }
        override fun extendLock(lock: LockHandle, ttl: Duration): Boolean = true
    }

    class RecordingTransactionManager : AbstractPlatformTransactionManager() {
        override fun doGetTransaction(): Any = Any()
        override fun doBegin(transaction: Any, definition: TransactionDefinition) { Events.list += "tx.begin" }
        override fun doCommit(status: DefaultTransactionStatus) { Events.list += "tx.commit" }
        override fun doRollback(status: DefaultTransactionStatus) { Events.list += "tx.rollback" }
    }

    /** 레이트리밋 자리(+11)를 흉내 낸다 — 실제 캐시 애스펙트는 Redis 가 필요해 순서 값만 같은 기록용 애스펙트를 쓴다. */
    @Aspect
    @Order(CacheAspectOrder.RATE_LIMITED)
    class RateLimitedSlot {
        @Around("@annotation(org.whiteprint.platform.core.lock.annotation.DistributedLock)")
        fun around(joinPoint: ProceedingJoinPoint): Any? {
            Events.add("rateLimited.before")
            try { return joinPoint.proceed() } finally { Events.add("rateLimited.after") }
        }
    }

    /** 중복 스킵 자리(+13). */
    @Aspect
    @Order(CacheAspectOrder.DEDUPLICATED)
    class DeduplicatedSlot {
        @Around("@annotation(org.whiteprint.platform.core.lock.annotation.DistributedLock)")
        fun around(joinPoint: ProceedingJoinPoint): Any? {
            Events.add("deduplicated.before")
            try { return joinPoint.proceed() } finally { Events.add("deduplicated.after") }
        }
    }

    open class OrderService {
        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 100)
        @Transactional
        open fun confirm(@DistributedLockKey id: Long): String {
            Events.add("method")
            return "ok"
        }

        @DistributedLock(prefix = "test:order", ttl = 1000, wait = 100)
        @Transactional
        open fun fail(@DistributedLockKey id: Long): String {
            Events.add("method")
            throw IllegalStateException("boom")
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @EnableTransactionManagement(proxyTargetClass = true)
    open class TestConfiguration {
        @Bean open fun transactionManager() = RecordingTransactionManager()
        @Bean open fun lockOperations() = RecordingLockOperations()
        @Bean open fun watchdog(lockOperations: RecordingLockOperations) = DistributedLockWatchdog(lockOperations)
        @Bean open fun distributedLockAspect(lockOperations: RecordingLockOperations, watchdog: DistributedLockWatchdog) =
            DistributedLockAspect(lockOperations, { UUID.randomUUID().toString() }, watchdog)
        @Bean open fun rateLimitedSlot() = RateLimitedSlot()
        @Bean open fun deduplicatedSlot() = DeduplicatedSlot()
        @Bean open fun orderService() = OrderService()
    }

    private val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)

    @AfterEach
    fun tearDown() {
        context.close()
        Events.list.clear()
    }

    @Test
    @DisplayName("커밋: 락은 트랜잭션 시작 전에 잡고 커밋 후에 푼다. 레이트리밋 안쪽, 중복 스킵 바깥")
    fun lockWrapsTransactionOnCommit() {
        context.getBean(OrderService::class.java).confirm(1)

        println("[order] ${Events.list}")
        assertEquals(
            listOf(
                "rateLimited.before(tx=false)",
                "lock.acquire(tx=false)",
                "deduplicated.before(tx=false)",
                "tx.begin",
                "method(tx=true)",
                "tx.commit",
                "deduplicated.after(tx=false)",
                "lock.release(tx=false)",
                "rateLimited.after(tx=false)",
            ),
            Events.list.toList(),
        )
    }

    @Test
    @DisplayName("롤백: 롤백이 끝난 뒤에 락을 푼다")
    fun lockWrapsTransactionOnRollback() {
        assertThrows(IllegalStateException::class.java) { context.getBean(OrderService::class.java).fail(1) }

        println("[order] ${Events.list}")
        val events = Events.list.toList()
        assertEquals(true, events.indexOf("tx.rollback") in 0 until events.indexOf("lock.release(tx=false)"))
        assertEquals(true, events.indexOf("lock.acquire(tx=false)") < events.indexOf("tx.begin"))
    }

}
