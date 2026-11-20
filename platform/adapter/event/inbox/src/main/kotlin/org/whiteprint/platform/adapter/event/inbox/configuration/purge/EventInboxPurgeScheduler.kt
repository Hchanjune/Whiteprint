package org.whiteprint.platform.adapter.event.inbox.configuration.purge

import io.github.hchanjune.omk.core.annotations.ManagedSchedule
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.adapter.event.inbox.configuration.EventInboxAutoConfigurationProperties
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Duration
import java.time.Instant

/**
 * inbox 정리(purge) — COMPLETED 행 중 retention 이 지난 것을 배치로 지운다.
 *
 * - 배치([EventInboxAutoConfigurationProperties.Purge.batchSize])가 꽉 차면 빌 때까지 이어서 반복하고,
 *   한 번 실행의 반복 상한([EventInboxAutoConfigurationProperties.Purge.maxBatchesPerRun])을 넘으면 다음 실행에 넘긴다.
 * - 배치마다 트랜잭션이 따로라 한 번에 큰 락·WAL 을 만들지 않는다.
 * - 여러 인스턴스가 동시에 돌아도 삭제라 결과가 같다 — 분산 락이 필요 없다.
 */
open class EventInboxPurgeScheduler(
    private val store: EventInboxStore,
    private val properties: EventInboxAutoConfigurationProperties,
) {

    init {
        val purge = properties.purge
        require(purge.retention >= Duration.ofDays(1)) {
            "adapter.event.inbox.purge.retention must be at least 1 day: ${purge.retention}"
        }
        require(purge.batchSize > 0) { "adapter.event.inbox.purge.batch-size must be positive: ${purge.batchSize}" }
        require(purge.maxBatchesPerRun > 0) { "adapter.event.inbox.purge.max-batches-per-run must be positive: ${purge.maxBatchesPerRun}" }
    }

    /** 지운 건수를 돌려준다 — 지울 게 없는 실행은 로그·trace 를 남기지 않는다. */
    @Scheduled(
        cron = "\${adapter.event.inbox.purge.cron:0 10 4 * * *}",
        zone = "\${adapter.event.inbox.purge.zone:UTC}",
    )
    @ManagedSchedule(quietWhenEmpty = true)
    open fun purge(): Int {
        val purge = properties.purge
        val olderThan = Instant.now().minus(purge.retention)
        var total = 0
        repeat(purge.maxBatchesPerRun) {
            val deleted = store.purgeCompleted(olderThan, purge.batchSize)
            total += deleted
            if (deleted < purge.batchSize) {
                if (total > 0) log.info("Inbox purge done — deleted={}, olderThan={}", total, olderThan)
                return total
            }
        }
        log.info("Inbox purge reached max batches — deleted={}, rest continues next run", total)
        return total
    }

    companion object {
        private val log = LoggerFactory.getLogger(EventInboxPurgeScheduler::class.java)
    }

}
