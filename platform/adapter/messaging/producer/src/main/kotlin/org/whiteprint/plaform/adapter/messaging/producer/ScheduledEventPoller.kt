package org.whiteprint.plaform.adapter.messaging.producer

import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.producer.EventPoller
import org.whiteprint.platform.core.messaging.producer.EventProducer

open class ScheduledEventPoller(
    private val outboxEventStore: EventOutboxStore,
    private val eventEnveloper: EventEnveloper,
    private val producer: EventProducer,
): EventPoller {

    @Transactional
    override fun pollOnce(): Int {
        val events = outboxEventStore.lockPending(limit = 100)

        events.forEach { event ->
            val envelope = eventEnveloper.envelope(event)
            try {
                producer.produce(envelope)
                outboxEventStore.markPublished(envelope.eventId)
            } catch (exception: Exception) {
                outboxEventStore.markFailed(envelope.eventId)
            }
        }

        return events.size
    }

}