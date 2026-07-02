package org.whiteprint.platform.adapter.event.publisher.poller

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.publisher.EventPoller
import org.whiteprint.platform.core.messaging.publisher.EventPublisher
import java.time.Instant

open class ScheduledEventPoller(
    private val outboxEventStore: EventOutboxStore,
    private val eventEnveloper: EventEnveloper,
    private val producer: EventPublisher,
    private val claimTimeoutMillis: Long = 300_000L,
) : EventPoller {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    override fun pollOnce(): Int {
        val events = outboxEventStore.claimPending(limit = 100)

        events.forEach { event ->
            try {
                val envelope = eventEnveloper.envelope(event)
                producer.publish(envelope)
                markPublishedWithRetry(event.eventId)
            } catch (e: Exception) {
                logger.warn("이벤트 발행 실패: ${event.eventId}", e)
                safeMarkFailed(event.eventId)
            }
        }

        return events.size
    }

    @Scheduled(fixedDelay = 60_000)
    fun recoverStaleProcessing() {
        val threshold = Instant.now().minusMillis(claimTimeoutMillis)
        outboxEventStore.resetStaleProcessing(threshold)
    }

    private fun markPublishedWithRetry(eventId: Long) {
        repeat(3) { attempt ->
            try {
                outboxEventStore.markPublished(eventId)
                return
            } catch (e: Exception) {
                if (attempt == 2) {
                    logger.error("markPublished 최종 실패, 유령으로 남김: $eventId", e)
                    return
                }
                logger.warn("markPublished 실패 (${attempt + 1}/3): $eventId")
                Thread.sleep(50L)
            }
        }
    }

    private fun safeMarkFailed(eventId: Long) {
        runCatching { outboxEventStore.markFailed(eventId) }
            .onFailure { logger.error("markFailed 실패: $eventId", it) }
    }
}