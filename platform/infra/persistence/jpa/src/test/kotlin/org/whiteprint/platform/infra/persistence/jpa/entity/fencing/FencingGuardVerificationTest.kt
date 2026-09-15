package org.whiteprint.platform.infra.persistence.jpa.entity.fencing

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.support.TransactionTemplate
import org.whiteprint.platform.core.kernel.policy.exception.StandardException
import org.whiteprint.platform.core.lock.context.LockContext
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.policy.LockPolicy
import org.whiteprint.platform.infra.persistence.jpa.entity.withId
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicLong

/**
 * `@FencingGuarded` 검사를 실제 DB 로 실측한다 — `@PreUpdate` 에서 바꾼 필드가 UPDATE 에 실리는지,
 * 콜백에서 던진 예외가 어떤 형태로 호출부에 오는지는 코드 리딩으로 확정할 수 없다.
 *
 * 로컬 Postgres 에 전용 DB(whiteprint_jpa_fencing_test)를 만들어 돈다. 접속 실패 시 skip.
 * 접속 override: JPA_VERSION_HOST / JPA_VERSION_PORT / JPA_VERSION_USER / JPA_VERSION_PASSWORD
 */
@EnabledIf("postgresAvailable")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FencingGuardVerificationTest @Autowired constructor(
    private val guardedRepository: GuardedItemRepository,
    private val unguardedRepository: UnguardedItemRepository,
    private val transactionTemplate: TransactionTemplate,
) {

    companion object {
        private val host = System.getenv("JPA_VERSION_HOST") ?: "192.168.0.12"
        private val port = System.getenv("JPA_VERSION_PORT") ?: "5432"
        private val user = System.getenv("JPA_VERSION_USER") ?: "postgres"
        private val password = System.getenv("JPA_VERSION_PASSWORD") ?: "postgres"
        private const val TEST_DB = "whiteprint_jpa_fencing_test"

        private val adminUrl = "jdbc:postgresql://$host:$port/postgres"
        private val testUrl = "jdbc:postgresql://$host:$port/$TEST_DB"

        private val ids = AtomicLong(System.currentTimeMillis())

        @JvmStatic
        fun postgresAvailable(): Boolean = try {
            DriverManager.getConnection(adminUrl, user, password).use { connection ->
                connection.createStatement().use { statement ->
                    val exists = statement
                        .executeQuery("SELECT 1 FROM pg_database WHERE datname = '$TEST_DB'")
                        .use { it.next() }
                    if (!exists) statement.executeUpdate("CREATE DATABASE $TEST_DB")
                }
            }
            true
        } catch (e: Exception) {
            false
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { testUrl }
            registry.add("spring.datasource.username") { user }
            registry.add("spring.datasource.password") { password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    private fun lock(token: Long) = LockHandle(LockKey("test:fencing"), "owner-$token", token)

    private fun <T> underLock(vararg tokens: Long, block: () -> T): T {
        val locks = tokens.map(::lock)
        locks.forEach(LockContext::push)
        return try {
            block()
        } finally {
            locks.reversed().forEach(LockContext::pop)
        }
    }

    private fun <T> tx(block: () -> T): T = transactionTemplate.execute { block() }!!

    private fun createGuarded(): Long {
        val id = ids.incrementAndGet()
        tx { guardedRepository.saveAndFlush(GuardedItem(label = "created").withId(id)) }
        return id
    }

    private fun renameGuarded(id: Long, label: String) =
        tx {
            val item = guardedRepository.findById(id).orElseThrow()
            item.label = label
            guardedRepository.saveAndFlush(item)
        }

    private fun tokenOf(id: Long): Long = tx { guardedRepository.findById(id).orElseThrow().lastFencingToken }

    /** 예외 사슬 어디에든 펜싱 거절이 있는지 — 콜백 예외가 감싸져 올 수 있다. */
    private fun Throwable.isFencingRejected(): Boolean =
        generateSequence(this) { it.cause }.any { it is StandardException && it.code == LockPolicy.FENCING_TOKEN_REJECTED.code }

    @AfterEach
    fun clearContext() {
        // 실패한 테스트가 스레드 로컬을 남기지 않게.
        repeat(5) { LockContext.currentFencingToken()?.let { LockContext.pop(lock(it)) } }
    }

    @Test
    @DisplayName("락 구간 안에서 만든 행에 토큰이 기록된다")
    fun insertUnderLockStampsToken() {
        val id = ids.incrementAndGet()
        underLock(7) { tx { guardedRepository.saveAndFlush(GuardedItem(label = "x").withId(id)) } }
        assertEquals(7, tokenOf(id))
    }

    @Test
    @DisplayName("더 새 토큰의 갱신은 통과하고 그 토큰을 기록한다")
    fun newerTokenIsAcceptedAndStamped() {
        val id = createGuarded()
        underLock(10) { renameGuarded(id, "first") }
        underLock(11) { renameGuarded(id, "second") }
        assertEquals(11, tokenOf(id))
    }

    @Test
    @DisplayName("같은 토큰으로 다시 쓰는 것은 통과한다(같은 보유자의 연속 쓰기)")
    fun sameTokenIsAccepted() {
        val id = createGuarded()
        underLock(20) {
            renameGuarded(id, "a")
            renameGuarded(id, "b")
        }
        assertEquals(20, tokenOf(id))
    }

    @Test
    @DisplayName("옛 토큰의 갱신은 거절되고 행은 바뀌지 않는다")
    fun olderTokenIsRejected() {
        val id = createGuarded()
        underLock(30) { renameGuarded(id, "new-holder") }

        val error = runCatching { underLock(29) { renameGuarded(id, "stale-holder") } }.exceptionOrNull()

        assertTrue(error != null && error.isFencingRejected(), "expected fencing rejection but was: $error")
        println("[fencing] rejection surfaced as: ${error!!::class.qualifiedName} -> root ${generateSequence(error) { it.cause }.last()::class.simpleName}")
        tx {
            val item = guardedRepository.findById(id).orElseThrow()
            assertEquals("new-holder", item.label)
            assertEquals(30, item.lastFencingToken)
        }
    }

    @Test
    @DisplayName("락 구간 밖의 쓰기는 검사도 기록도 하지 않는다")
    fun writeOutsideLockSkipsGuard() {
        val id = createGuarded()
        underLock(40) { renameGuarded(id, "locked") }
        renameGuarded(id, "unlocked")
        assertEquals(40, tokenOf(id))
    }

    @Test
    @DisplayName("어노테이션이 없는 엔티티는 락 구간 안에서도 기록하지 않는다")
    fun unguardedEntityIsIgnored() {
        val id = ids.incrementAndGet()
        underLock(50) { tx { unguardedRepository.saveAndFlush(UnguardedItem(label = "x").withId(id)) } }
        underLock(1) {
            tx {
                val item = unguardedRepository.findById(id).orElseThrow()
                item.label = "y"
                unguardedRepository.saveAndFlush(item)
            }
        }
        assertEquals(0, tx { unguardedRepository.findById(id).orElseThrow().lastFencingToken })
    }

    @Test
    @DisplayName("겹쳐 쥔 락은 가장 오래된 토큰으로 판단한다")
    fun nestedLocksUseOldestToken() {
        val id = createGuarded()
        underLock(60) { renameGuarded(id, "holder") }

        // 바깥 락(59)은 이미 잃었고 안쪽에서 새 락(61)을 잡은 옛 보유자 — 통과하면 안 된다.
        val error = runCatching { underLock(59, 61) { renameGuarded(id, "stale-with-inner") } }.exceptionOrNull()
        assertTrue(error != null && error.isFencingRejected(), "expected fencing rejection but was: $error")
    }

    @Test
    @DisplayName("새 보유자 쓰기 전에 읽어둔 옛 보유자는 버전 충돌로 막힌다(검사와 @Version 의 결합)")
    fun staleReadIsStoppedByVersion() {
        val id = createGuarded()
        val staleCopy = tx { guardedRepository.findById(id).orElseThrow() }

        underLock(71) { renameGuarded(id, "new-holder") }

        staleCopy.label = "stale-holder"
        val error = runCatching { underLock(72) { tx { guardedRepository.saveAndFlush(staleCopy) } } }.exceptionOrNull()
        assertTrue(
            error is ObjectOptimisticLockingFailureException || generateSequence(error) { it.cause }.any { it::class.simpleName?.contains("StaleObject") == true },
            "expected optimistic lock failure but was: $error",
        )
    }

}
