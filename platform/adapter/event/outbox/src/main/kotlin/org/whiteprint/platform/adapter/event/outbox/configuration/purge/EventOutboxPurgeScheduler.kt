package org.whiteprint.platform.adapter.event.outbox.configuration.purge

import io.github.hchanjune.omk.core.annotations.ManagedSchedule
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.adapter.event.outbox.configuration.EventOutboxAutoConfigurationProperties
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import java.time.Duration
import java.time.Instant

/**
 * outbox 정리(purge) — PUBLISHED 행 중 retention 이 지난 것을 배치로 지운다.
 *
 * - 배치([EventOutboxAutoConfigurationProperties.Purge.batchSize])가 꽉 차면 빌 때까지 이어서 반복하고,
 *   한 번 실행의 반복 상한([EventOutboxAutoConfigurationProperties.Purge.maxBatchesPerRun])을 넘으면 다음 실행에 넘긴다.
 * - 배치마다 트랜잭션이 따로라 한 번에 큰 락·WAL 을 만들지 않는다.
 * - 여러 인스턴스가 동시에 돌아도 삭제라 결과가 같다 — 분산 락이 필요 없다.
 */
open class EventOutboxPurgeScheduler(
    private val store: EventOutboxStore,
    private val properties: EventOutboxAutoConfigurationProperties,
) {

    init {
        val purge = properties.purge
        require(purge.retention >= Duration.ofDays(1)) {
            "adapter.event.outbox.purge.retention must be at least 1 day: ${purge.retention}"
        }
        require(purge.batchSize > 0) { "adapter.event.outbox.purge.batch-size must be positive: ${purge.batchSize}" }
        require(purge.maxBatchesPerRun > 0) { "adapter.event.outbox.purge.max-batches-per-run must be positive: ${purge.maxBatchesPerRun}" }
    }

    /** 지운 건수를 돌려준다 — 지울 게 없는 실행은 로그·trace 를 남기지 않는다. */
    @Scheduled(
        cron = "\${adapter.event.outbox.purge.cron:0 0 4 * * *}",
        zone = "\${adapter.event.outbox.purge.zone:UTC}",
    )
    @ManagedSchedule(quietWhenEmpty = true)
    open fun purge(): Int {
        val purge = properties.purge
        val olderThan = Instant.now().minus(purge.retention)
        var total = 0
        repeat(purge.maxBatchesPerRun) {
            val deleted = store.purgePublished(olderThan, purge.batchSize)
            total += deleted
            if (deleted < purge.batchSize) {
                if (total > 0) log.info("Outbox purge done — deleted={}, olderThan={}", total, olderThan)
                return total
            }
        }
        log.info("Outbox purge reached max batches — deleted={}, rest continues next run", total)
        return total
    }

    companion object {
        private val log = LoggerFactory.getLogger(EventOutboxPurgeScheduler::class.java)
    }

}
