package org.whiteprint.platform.adapter.event.inbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.whiteprint.platform.adapter.event.inbox.configuration.EventInboxAutoConfigurationProperties
import org.whiteprint.platform.adapter.event.inbox.configuration.purge.EventInboxPurgeScheduler
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Duration
import java.time.Instant

/** 인박스 정리 스케줄러 — 배치 반복·상한·설정 검증. */
class EventInboxPurgeSchedulerTest {

    private val store: EventInboxStore = mock(EventInboxStore::class.java)

    private fun properties(batchSize: Int = 100, maxBatches: Int = 5, retention: Duration = Duration.ofDays(14)) =
        EventInboxAutoConfigurationProperties(
            purge = EventInboxAutoConfigurationProperties.Purge(
                enabled = true, retention = retention, batchSize = batchSize, maxBatchesPerRun = maxBatches,
            ),
        )

    private fun anyInstant(): Instant = any(Instant::class.java) ?: Instant.EPOCH

    @Test
    @DisplayName("배치가 덜 차면 한 번으로 끝난다")
    fun stopsWhenBatchNotFull() {
        `when`(store.purgeCompleted(anyInstant(), anyInt())).thenReturn(40)

        val deleted = EventInboxPurgeScheduler(store, properties()).purge()

        assertEquals(40, deleted)
        verify(store, times(1)).purgeCompleted(anyInstant(), anyInt())
    }

    @Test
    @DisplayName("배치가 꽉 차면 빌 때까지 이어서 지운다")
    fun repeatsWhileBatchFull() {
        `when`(store.purgeCompleted(anyInstant(), anyInt())).thenReturn(100, 100, 30)

        val deleted = EventInboxPurgeScheduler(store, properties()).purge()

        assertEquals(230, deleted)
        verify(store, times(3)).purgeCompleted(anyInstant(), anyInt())
    }

    @Test
    @DisplayName("반복 상한에 닿으면 멈추고 남은 건 다음 실행에 넘긴다")
    fun stopsAtMaxBatches() {
        `when`(store.purgeCompleted(anyInstant(), anyInt())).thenReturn(100)

        val deleted = EventInboxPurgeScheduler(store, properties(maxBatches = 3)).purge()

        assertEquals(300, deleted)
        verify(store, times(3)).purgeCompleted(anyInstant(), anyInt())
    }

    @Test
    @DisplayName("retention 이 1일 미만이면 기동 시점에 실패한다")
    fun rejectsShortRetention() {
        val error = assertThrows<IllegalArgumentException> {
            EventInboxPurgeScheduler(store, properties(retention = Duration.ofHours(1)))
        }
        assertTrue(error.message!!.contains("retention"))
    }
}
