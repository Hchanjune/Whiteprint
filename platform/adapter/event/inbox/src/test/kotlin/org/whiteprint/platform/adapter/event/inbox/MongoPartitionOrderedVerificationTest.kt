package org.whiteprint.platform.adapter.event.inbox

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
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
import org.mockito.Mockito
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer.MongoEventInboxStore
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxKeyLockDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.repository.MongoEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.model.event.EventScope
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
 * PARTITION_ORDERED 동시성 실측 검증 — Mongo 판 (partition-ordered-design.md Phase 5).
 *
 * Postgres 판([PartitionOrderedJdbcVerificationTest])과 달리 SQL 복제가 아니라
 * **실제 [MongoEventInboxStore] 프로덕션 코드**를 MongoTemplate 직생성으로 검증한다
 * (repository 는 save() 전용이라 mock). 검증 시나리오 5종은 Postgres 판과 동일.
 *
 * 접속 실패 시 skip. override: INBOX_PO_MONGO_HOST / _PORT / _USER / _PASSWORD
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoPartitionOrderedVerificationTest {

    private val host = System.getenv("INBOX_PO_MONGO_HOST") ?: "192.168.0.12"
    private val port = System.getenv("INBOX_PO_MONGO_PORT") ?: "27017"
    private val user = System.getenv("INBOX_PO_MONGO_USER") ?: "mongo"
    private val password = System.getenv("INBOX_PO_MONGO_PASSWORD") ?: "mongo"
    private val testDb = "whiteprint_inbox_po_test"

    private var available = false
    private lateinit var mongoClient: MongoClient
    private lateinit var template: MongoTemplate
    private lateinit var store: MongoEventInboxStore
    private val idSeq = AtomicLong(1_000)

    @BeforeAll
    fun setUp() {
        available = runCatching {
            mongoClient = MongoClients.create(
                "mongodb://$user:$password@$host:$port/?authSource=admin&serverSelectionTimeoutMS=3000"
            )
            mongoClient.getDatabase(testDb).runCommand(Document("ping", 1))
            template = MongoTemplate(SimpleMongoClientDatabaseFactory(mongoClient, testDb))
            store = MongoEventInboxStore(
                repository = Mockito.mock(MongoEventInboxRepository::class.java),
                mongoTemplate = template,
            )
        }.isSuccess
    }

    @AfterAll
    fun tearDown() {
        if (!available) return
        runCatching {
            mongoClient.getDatabase(testDb).drop()
            mongoClient.close()
        }
    }

    @BeforeEach
    fun truncate() {
        assumeTrue(available, "로컬 Mongo($host:$port) 접속 불가 — 검증 skip")
        template.remove(Query(), EventInboxDocument::class.java)
        template.remove(Query(), EventInboxKeyLockDocument::class.java)
    }

    private fun insertReceived(eventId: Long, partitionKey: Long, eventType: String = "TestEvent") {
        template.insert(
            EventInboxDocument(
                eventId = eventId,
                traceId = "trace",
                causationId = null,
                occurredAt = Instant.now(),
                issuer = "test",
                producer = "test",
                schemaVersion = "v1",
                partitionKey = partitionKey,
                eventScope = EventScope.EXTERNAL,
                eventType = eventType,
                payload = ByteArray(0),
                payloadJson = "{}",
                metadataJson = "{}",
                status = EventInboxStatus.RECEIVED,
                receivedAt = Instant.now(),
                processedAt = null,
                attemptCount = 0,
                lastAttemptedAt = null,
                errorMessage = null,
            )
        )
    }

    private fun setStatus(eventId: Long, status: EventInboxStatus) {
        template.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", status),
            EventInboxDocument::class.java,
        )
    }

    private fun frontiers(limit: Int = 100): List<Pair<Long, Long>> =
        store.findClaimableFrontiers("TestEvent", limit).map { it.eventId to it.partitionKey }

    // ---------- 검증 (Postgres 판과 동일 시나리오) ----------

    @Test
    @DisplayName("① 같은 키의 서로 다른 두 이벤트 — 두 스레드 동시 claim 시 최대 1건만 성공")
    fun concurrentClaimRace() {
        val rounds = 50
        repeat(rounds) { round ->
            val key = 10_000L + round
            val e1 = idSeq.incrementAndGet()
            val e2 = idSeq.incrementAndGet()
            insertReceived(e1, key)
            insertReceived(e2, key)

            val barrier = CyclicBarrier(2)
            val results = arrayOfNulls<Boolean>(2)
            val threads = listOf(e1, e2).mapIndexed { i, eventId ->
                Thread {
                    barrier.await()
                    results[i] = store.tryAcquireOrdered(eventId, key)
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
        val events = (1..5).map { idSeq.incrementAndGet().also { id -> insertReceived(id, key) } }

        events.forEach { expected ->
            val front = frontiers()
            assertEquals(1, front.size)
            assertEquals(expected, front[0].first, "frontier 가 다음 순번이 아님")

            assertTrue(store.tryAcquireOrdered(expected, key))
            assertTrue(frontiers().isEmpty(), "PROCESSING 중인데 키가 frontier 에 노출됨")
            store.markCompleted(expected)
        }
    }

    @Test
    @DisplayName("③ FAILED 블로킹 — 키 정지, 타 키 정상, 복구 시 순서대로 재개")
    fun failedBlocksKey() {
        val failedKey = 30_000L
        val healthyKey = 30_001L
        val f1 = idSeq.incrementAndGet().also { insertReceived(it, failedKey) }
        val f2 = idSeq.incrementAndGet().also { insertReceived(it, failedKey) }
        val h1 = idSeq.incrementAndGet().also { insertReceived(it, healthyKey) }

        store.markFailed(f1, "boom")

        val front = frontiers()
        assertEquals(listOf(h1 to healthyKey), front, "FAILED 키가 frontier 에 노출되거나 타 키가 누락됨")
        assertFalse(store.tryAcquireOrdered(f2, failedKey), "FAILED 키의 후속 이벤트가 claim 됨")
        assertTrue(store.tryAcquireOrdered(h1, healthyKey), "타 키가 FAILED 에 영향을 받음")

        // 복구: FAILED → RECEIVED 리셋 시 f1 부터 순서대로 재개
        setStatus(f1, EventInboxStatus.RECEIVED)
        val recovered = frontiers()
        assertEquals(f1, recovered.single { it.second == failedKey }.first, "복구 후 첫 순번이 f1 이 아님")

        // DEAD(재시도 소진 종결)도 동일하게 키를 블로킹한다
        store.markDead(f1)
        assertTrue(frontiers().none { it.second == failedKey }, "DEAD 키가 frontier 에 노출됨")
        assertFalse(store.tryAcquireOrdered(f2, failedKey), "DEAD 키의 후속 이벤트가 claim 됨")
    }

    @Test
    @DisplayName("④ frontier 공정성 — 한 키의 대량 백로그가 윈도우를 침수시키지 않음")
    fun frontierFairness() {
        val hotKey = 40_000L
        repeat(200) { insertReceived(idSeq.incrementAndGet(), hotKey) }
        (1..5).forEach { insertReceived(idSeq.incrementAndGet(), 41_000L + it) }

        val front = frontiers(limit = 10)
        assertEquals(6, front.size, "키당 1건이 아님 (윈도우 침수)")
        assertEquals(1, front.count { it.second == hotKey }, "hot 키가 frontier 에 여러 건 노출됨")

        val hotFirst = front.first { it.second == hotKey }.first
        assertTrue(store.tryAcquireOrdered(hotFirst, hotKey))
        val whileBusy = frontiers(limit = 10)
        assertEquals(5, whileBusy.size)
        assertTrue(whileBusy.none { it.second == hotKey }, "busy 키가 frontier 에 노출됨")
    }

    @Test
    @DisplayName("⑤ 멀티 폴러 종합 — 2인스턴스 모사: 키별 완료 순서 유지 + 같은 키 동시 처리 0건")
    fun multiPollerSimulation() {
        val keys = listOf(50_001L, 50_002L, 50_003L, 50_004L, 50_005L, 50_006L)
        val seeded = ConcurrentHashMap<Long, List<Long>>()
        keys.forEach { key ->
            seeded[key] = (1..5).map { idSeq.incrementAndGet().also { id -> insertReceived(id, key) } }
        }
        val total = keys.size * 5

        val completionOrder = ConcurrentHashMap<Long, MutableList<Long>>()
        val inFlight = ConcurrentHashMap<Long, Long>()
        val sameKeyOverlap = AtomicBoolean(false)
        val completed = CountDownLatch(total)

        val pool = Executors.newVirtualThreadPerTaskExecutor()
        val pollers = (1..2).map {
            Thread {
                val slots = Semaphore(3)
                val deadline = System.currentTimeMillis() + 30_000
                while (completed.count > 0 && System.currentTimeMillis() < deadline) {
                    val free = slots.availablePermits()
                    if (free <= 0) { Thread.sleep(10); continue }
                    for ((eventId, key) in frontiers(limit = free * 3)) {
                        if (!slots.tryAcquire()) break
                        if (!store.tryAcquireOrdered(eventId, key)) { slots.release(); continue }
                        pool.execute {
                            try {
                                if (inFlight.putIfAbsent(key, eventId) != null) sameKeyOverlap.set(true)
                                Thread.sleep(15)
                                completionOrder.computeIfAbsent(key) { mutableListOf() }
                                    .let { list -> synchronized(list) { list.add(eventId) } }
                                store.markCompleted(eventId)
                                inFlight.remove(key)
                                completed.countDown()
                            } finally {
                                slots.release()
                            }
                        }
                    }
                    Thread.sleep(20)
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
