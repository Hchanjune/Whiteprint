package org.whiteprint.platform.adapter.event.inbox

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * PARTITION_ORDERED 동시성 실측 검증 (partition-ordered-design.md Phase 5).
 *
 * 로컬 Postgres 에 전용 DB(whiteprint_inbox_po_test)를 만들어,
 * [JpaEventInboxRepository] 와 동일한 SQL 로 다음을 검증한다:
 *  1. 같은 키의 서로 다른 두 이벤트를 두 스레드(=두 인스턴스)가 동시에 claim 해도 최대 1건만 성공
 *  2. 키 내 처리는 event_id 시간순으로만 진행(PROCESSING 중엔 frontier 에서 키 제외)
 *  3. FAILED 는 해당 키를 블로킹하고, RECEIVED 복구 시 순서대로 재개
 *  4. 한 키의 대량 백로그가 frontier 윈도우를 침수시키지 않음(기아 방지)
 *  5. 멀티 폴러(2인스턴스 모사) 종합 — 키별 완료 순서 == 적재 순서, 같은 키 동시 처리 0건
 *
 * ⚠️ 이 테스트의 SQL 은 JpaEventInboxRepository 의 네이티브 쿼리와 동일해야 한다.
 * 접속 실패 시 skip 된다. 접속 override: INBOX_PO_HOST / INBOX_PO_PORT / INBOX_PO_USER / INBOX_PO_PASSWORD
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartitionOrderedJdbcVerificationTest {

    private val host = System.getenv("INBOX_PO_HOST") ?: "192.168.0.12"
    private val port = System.getenv("INBOX_PO_PORT") ?: "5432"
    private val user = System.getenv("INBOX_PO_USER") ?: "postgres"
    private val password = System.getenv("INBOX_PO_PASSWORD") ?: "postgres"
    private val testDb = "whiteprint_inbox_po_test"

    private val adminUrl = "jdbc:postgresql://$host:$port/postgres"
    private val testUrl = "jdbc:postgresql://$host:$port/$testDb"

    private var available = false
    private val idSeq = AtomicLong(1_000)

    // ---------- 리포지토리와 동일한 SQL ----------

    private val claimOrderedSql = """
        UPDATE event_inbox
        SET status = 'PROCESSING',
            attempt_count = attempt_count + 1,
            last_attempted_at = now()
        WHERE event_id = ?
          AND status = 'RECEIVED'
          AND NOT EXISTS (
              SELECT 1 FROM event_inbox b
              WHERE b.partition_key = ?
                AND b.status IN ('PROCESSING', 'FAILED')
          )
    """.trimIndent()

    private val frontierSql = """
        SELECT f.* FROM (
            SELECT DISTINCT ON (partition_key) *
            FROM event_inbox
            WHERE event_type = ?
              AND status = 'RECEIVED'
            ORDER BY partition_key, event_id
        ) f
        WHERE NOT EXISTS (
            SELECT 1 FROM event_inbox b
            WHERE b.partition_key = f.partition_key
              AND b.status IN ('PROCESSING', 'FAILED')
        )
        ORDER BY f.event_id
        LIMIT ?
    """.trimIndent()

    // ---------- 셋업 ----------

    @BeforeAll
    fun createTestDatabase() {
        available = runCatching {
            DriverManager.getConnection(adminUrl, user, password).use { admin ->
                admin.createStatement().use {
                    it.execute("DROP DATABASE IF EXISTS $testDb WITH (FORCE)")
                    it.execute("CREATE DATABASE $testDb")
                }
            }
            DriverManager.getConnection(testUrl, user, password).use { conn ->
                conn.createStatement().use {
                    it.execute(
                        """
                        CREATE TABLE event_inbox (
                            event_id BIGINT PRIMARY KEY,
                            trace_id VARCHAR(255) NOT NULL,
                            causation_id VARCHAR(255),
                            occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            issuer VARCHAR(255) NOT NULL,
                            producer VARCHAR(255) NOT NULL,
                            schema_version VARCHAR(255) NOT NULL,
                            partition_key BIGINT NOT NULL,
                            event_scope VARCHAR(50) NOT NULL,
                            event_type VARCHAR(255) NOT NULL,
                            payload BYTEA NOT NULL,
                            payload_json TEXT NOT NULL,
                            metadata_json TEXT NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            received_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            processed_at TIMESTAMP WITH TIME ZONE,
                            attempt_count INT NOT NULL,
                            last_attempted_at TIMESTAMP WITH TIME ZONE,
                            error_message TEXT
                        )
                        """.trimIndent()
                    )
                    it.execute("CREATE INDEX idx_po_test_pk_status ON event_inbox(partition_key, status)")
                    it.execute("CREATE INDEX idx_po_test_type_status ON event_inbox(event_type, status)")
                }
            }
        }.isSuccess
    }

    @AfterAll
    fun dropTestDatabase() {
        if (!available) return
        runCatching {
            DriverManager.getConnection(adminUrl, user, password).use { admin ->
                admin.createStatement().use { it.execute("DROP DATABASE IF EXISTS $testDb WITH (FORCE)") }
            }
        }
    }

    @BeforeEach
    fun truncate() {
        assumeTrue(available, "로컬 Postgres($host:$port) 접속 불가 — 검증 skip")
        connection().use { it.createStatement().execute("TRUNCATE event_inbox") }
    }

    private fun connection(): Connection =
        DriverManager.getConnection(testUrl, user, password)

    private fun insertReceived(conn: Connection, eventId: Long, partitionKey: Long, eventType: String = "TestEvent") {
        conn.prepareStatement(
            """
            INSERT INTO event_inbox (event_id, trace_id, causation_id, occurred_at, issuer, producer,
                schema_version, partition_key, event_scope, event_type, payload, payload_json,
                metadata_json, status, received_at, attempt_count)
            VALUES (?, 'trace', null, now(), 'test', 'test', 'v1', ?, 'EXTERNAL', ?, ?, '{}', '{}', 'RECEIVED', now(), 0)
            """.trimIndent()
        ).use {
            it.setLong(1, eventId)
            it.setLong(2, partitionKey)
            it.setString(3, eventType)
            it.setBytes(4, ByteArray(0))
            it.executeUpdate()
        }
    }

    /** JpaEventInboxStore.tryAcquireOrdered 와 동일 — advisory lock 과 게이트 CAS 를 한 트랜잭션에서. */
    private fun tryAcquireOrdered(conn: Connection, eventId: Long, partitionKey: Long): Boolean {
        conn.autoCommit = false
        try {
            val locked = conn.prepareStatement("SELECT pg_try_advisory_xact_lock(?)").use {
                it.setLong(1, partitionKey)
                it.executeQuery().let { rs -> rs.next(); rs.getBoolean(1) }
            }
            if (!locked) return false
            return conn.prepareStatement(claimOrderedSql).use {
                it.setLong(1, eventId)
                it.setLong(2, partitionKey)
                it.executeUpdate() > 0
            }
        } finally {
            conn.commit()
            conn.autoCommit = true
        }
    }

    private fun frontiers(conn: Connection, eventType: String = "TestEvent", limit: Int = 100): List<Pair<Long, Long>> =
        conn.prepareStatement(frontierSql).use {
            it.setString(1, eventType)
            it.setInt(2, limit)
            val rs = it.executeQuery()
            buildList { while (rs.next()) add(rs.getLong("event_id") to rs.getLong("partition_key")) }
        }

    private fun setStatus(conn: Connection, eventId: Long, status: String) {
        conn.prepareStatement("UPDATE event_inbox SET status = ? WHERE event_id = ?").use {
            it.setString(1, status)
            it.setLong(2, eventId)
            it.executeUpdate()
        }
    }

    // ---------- 검증 ----------

    @Test
    @DisplayName("① 같은 키의 서로 다른 두 이벤트 — 두 스레드 동시 claim 시 최대 1건만 성공")
    fun concurrentClaimRace() {
        val rounds = 50
        repeat(rounds) { round ->
            val key = 10_000L + round
            val e1 = idSeq.incrementAndGet()
            val e2 = idSeq.incrementAndGet()
            connection().use { insertReceived(it, e1, key); insertReceived(it, e2, key) }

            val barrier = CyclicBarrier(2)
            val results = arrayOfNulls<Boolean>(2)
            val threads = listOf(e1, e2).mapIndexed { i, eventId ->
                Thread {
                    connection().use { conn ->
                        barrier.await()
                        results[i] = tryAcquireOrdered(conn, eventId, key)
                    }
                }.also { it.start() }
            }
            threads.forEach { it.join() }

            val successes = results.count { it == true }
            assertTrue(successes <= 1, "round=$round: 같은 키의 두 이벤트가 동시에 claim 됨 (successes=$successes)")
        }
    }

    @Test
    @DisplayName("② 키 내 시간순 — PROCESSING 중엔 frontier 에서 키 제외, 완료 후 다음 순번만 claim")
    fun perKeyOrdering() {
        val key = 20_000L
        val events = (1..5).map { idSeq.incrementAndGet() }
        connection().use { conn -> events.forEach { insertReceived(conn, it, key) } }

        connection().use { conn ->
            events.forEach { expected ->
                val front = frontiers(conn)
                assertEquals(1, front.size)
                assertEquals(expected, front[0].first, "frontier 가 다음 순번이 아님")

                assertTrue(tryAcquireOrdered(conn, expected, key))
                // PROCESSING 동안 이 키는 frontier 에서 사라져야 한다
                assertTrue(frontiers(conn).isEmpty(), "PROCESSING 중인데 키가 frontier 에 노출됨")
                setStatus(conn, expected, "COMPLETED")
            }
        }
    }

    @Test
    @DisplayName("③ FAILED 블로킹 — 키 정지, 타 키 정상, 복구 시 순서대로 재개")
    fun failedBlocksKey() {
        val failedKey = 30_000L
        val healthyKey = 30_001L
        val f1 = idSeq.incrementAndGet()
        val f2 = idSeq.incrementAndGet()
        val h1 = idSeq.incrementAndGet()
        connection().use { conn ->
            insertReceived(conn, f1, failedKey)
            insertReceived(conn, f2, failedKey)
            insertReceived(conn, h1, healthyKey)
        }

        connection().use { conn ->
            setStatus(conn, f1, "FAILED")

            val front = frontiers(conn)
            assertEquals(listOf(h1 to healthyKey), front, "FAILED 키가 frontier 에 노출되거나 타 키가 누락됨")
            assertFalse(tryAcquireOrdered(conn, f2, failedKey), "FAILED 키의 후속 이벤트가 claim 됨")
            assertTrue(tryAcquireOrdered(conn, h1, healthyKey), "타 키가 FAILED 에 영향을 받음")

            // 복구: FAILED → RECEIVED 리셋 시 f1 부터 순서대로 재개
            setStatus(conn, f1, "RECEIVED")
            val recovered = frontiers(conn)
            assertEquals(f1, recovered.single { it.second == failedKey }.first, "복구 후 첫 순번이 f1 이 아님")
        }
    }

    @Test
    @DisplayName("④ frontier 공정성 — 한 키의 대량 백로그가 윈도우를 침수시키지 않음")
    fun frontierFairness() {
        val hotKey = 40_000L
        connection().use { conn ->
            repeat(200) { insertReceived(conn, idSeq.incrementAndGet(), hotKey) }
            (1..5).forEach { insertReceived(conn, idSeq.incrementAndGet(), 41_000L + it) }
        }

        connection().use { conn ->
            // hot 키가 idle 이면: hot 은 frontier 에 '1건만' + 나머지 5개 키 전부 보여야 함
            val front = frontiers(conn, limit = 10)
            assertEquals(6, front.size, "키당 1건이 아님 (윈도우 침수)")
            assertEquals(1, front.count { it.second == hotKey }, "hot 키가 frontier 에 여러 건 노출됨")

            // hot 키가 busy(PROCESSING)면: hot 제외, 5개 키만
            val hotFirst = front.first { it.second == hotKey }.first
            assertTrue(tryAcquireOrdered(conn, hotFirst, hotKey))
            val whileBusy = frontiers(conn, limit = 10)
            assertEquals(5, whileBusy.size)
            assertTrue(whileBusy.none { it.second == hotKey }, "busy 키가 frontier 에 노출됨")
        }
    }

    @Test
    @DisplayName("⑤ 멀티 폴러 종합 — 2인스턴스 모사: 키별 완료 순서 유지 + 같은 키 동시 처리 0건")
    fun multiPollerSimulation() {
        val keys = listOf(50_001L, 50_002L, 50_003L, 50_004L, 50_005L, 50_006L)
        val seeded = ConcurrentHashMap<Long, List<Long>>()
        connection().use { conn ->
            keys.forEach { key ->
                seeded[key] = (1..5).map { idSeq.incrementAndGet().also { id -> insertReceived(conn, id, key) } }
            }
        }
        val total = keys.size * 5

        val completionOrder = ConcurrentHashMap<Long, MutableList<Long>>()
        val inFlight = ConcurrentHashMap<Long, Long>() // key → eventId
        val sameKeyOverlap = AtomicBoolean(false)
        val completed = CountDownLatch(total)

        val pool = Executors.newVirtualThreadPerTaskExecutor()
        val pollers = (1..2).map { // 인스턴스 2개 모사 — 각자 슬롯 3
            Thread {
                val slots = Semaphore(3)
                connection().use { conn ->
                    val deadline = System.currentTimeMillis() + 30_000
                    while (completed.count > 0 && System.currentTimeMillis() < deadline) {
                        val free = slots.availablePermits()
                        if (free <= 0) { Thread.sleep(10); continue }
                        for ((eventId, key) in frontiers(conn, limit = free * 3)) {
                            if (!slots.tryAcquire()) break
                            if (!tryAcquireOrdered(conn, eventId, key)) { slots.release(); continue }
                            pool.execute {
                                try {
                                    if (inFlight.putIfAbsent(key, eventId) != null) sameKeyOverlap.set(true)
                                    Thread.sleep(15) // 처리 흉내
                                    completionOrder.computeIfAbsent(key) { mutableListOf() }
                                        .let { list -> synchronized(list) { list.add(eventId) } }
                                    connection().use { c -> setStatus(c, eventId, "COMPLETED") }
                                    inFlight.remove(key)
                                    completed.countDown()
                                } finally {
                                    slots.release()
                                }
                            }
                        }
                        Thread.sleep(20) // 폴 주기 축소판
                    }
                }
            }.also { it.start() }
        }

        assertTrue(completed.await(30, TimeUnit.SECONDS), "타임아웃 — 전체 이벤트가 소진되지 않음(잔여=${completed.count})")
        pollers.forEach { it.join(5_000) }
        pool.shutdown()

        assertFalse(sameKeyOverlap.get(), "같은 키의 이벤트가 동시에 처리됨")
        keys.forEach { key ->
            assertEquals(seeded[key], completionOrder[key], "key=$key 완료 순서가 적재 순서와 다름")
        }
    }
}
