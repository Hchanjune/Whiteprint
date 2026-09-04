package org.whiteprint.platform.core.kernel.identifier

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 식별자 유일성 실측.
 *
 * 랜덤 12비트이던 시절 이 테스트들은 전부 실패했다 —
 * 1000개 연속 생성에 120건 안팎이 겹쳤고, 100개만 만들어도 20번 중 17번 충돌이 났다.
 * 알림 팬아웃처럼 한 청크에 수백~수천 건을 만드는 경로에서 PK 충돌로 드러나는 종류의 버그다.
 */
class TsidGeneratorVerificationTest {

    @Test
    @DisplayName("한 밀리초에 몰아 만들어도 겹치지 않는다")
    fun burstHasNoDuplicates() {
        repeat(20) { attempt ->
            val ids = (1..1000).map { TsidGenerator.generate() }
            assertEquals(
                ids.size, ids.toSet().size,
                "시도 $attempt: 1000개 중 ${ids.size - ids.toSet().size}개가 겹쳤다",
            )
        }
    }

    @Test
    @DisplayName("밀리초당 상한(4096)을 넘겨도 다음 밀리초로 넘어가며 유일하다")
    fun exceedingPerMillisCapacityStillUnique() {
        // 4096 을 훌쩍 넘겨 시퀀스 소진 → 다음 밀리초 대기 경로를 실제로 태운다.
        val ids = (1..20_000).map { TsidGenerator.generate() }
        assertEquals(ids.size, ids.toSet().size, "시퀀스 소진 경로에서 겹쳤다")
    }

    @Test
    @DisplayName("생성 순서대로 증가한다 — keyset 페이지네이션이 이 정렬성에 기댄다")
    fun idsAreMonotonic() {
        val ids = (1..5_000).map { TsidGenerator.generate() }
        assertEquals(ids, ids.sorted(), "생성 순서와 정렬 순서가 다르다")
    }

    @Test
    @DisplayName("여러 스레드가 동시에 만들어도 겹치지 않는다")
    fun concurrentGenerationHasNoDuplicates() {
        val threads = 16
        val perThread = 2_000
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val seen = ConcurrentHashMap.newKeySet<Long>()
        val duplicates = java.util.concurrent.atomic.AtomicInteger()

        repeat(threads) {
            pool.submit {
                start.await()
                repeat(perThread) {
                    if (!seen.add(TsidGenerator.generate())) duplicates.incrementAndGet()
                }
            }
        }
        start.countDown()
        pool.shutdown()
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "생성이 끝나지 않았다")

        assertEquals(0, duplicates.get(), "동시 생성에서 ${duplicates.get()}건이 겹쳤다")
        assertEquals(threads * perThread, seen.size)
    }

    @Test
    @DisplayName("nodeId 가 다르면 절대 겹치지 않는다 — 인스턴스 간 유일성의 근거")
    fun differentNodeIdsNeverCollide() {
        val a = (1..3_000).map { TsidGenerator.generate(1L) }.toSet()
        val b = (1..3_000).map { TsidGenerator.generate(2L) }.toSet()
        assertTrue(a.intersect(b).isEmpty(), "nodeId 가 다른데 겹친 id 가 있다")
    }

    @Test
    @DisplayName("nodeId 는 10비트로 잘려 다른 자리 비트를 침범하지 않는다")
    fun nodeIdIsMaskedToItsOwnBits() {
        val sequenceBits = 12
        val nodeMask = (1L shl 10) - 1

        // 1024 는 10비트를 넘는 값이라 0 으로 잘려야 한다.
        val overflowed = (TsidGenerator.generate(1024L) ushr sequenceBits) and nodeMask
        assertEquals(0L, overflowed)

        val normal = (TsidGenerator.generate(7L) ushr sequenceBits) and nodeMask
        assertEquals(7L, normal)
    }
}
