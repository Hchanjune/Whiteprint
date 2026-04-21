package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.subscriber.EventHandler

abstract class AbstractEventHandler<E: Event>(
    private val inboxStore: EventInboxStore,
    private val eventSerializer: EventSerializer,
): EventHandler<E> {

    @Scheduled(fixedDelay = 500)
    open fun pollAndProcess() {
        val records = inboxStore.findAllByEventTypeAndStatus(
            eventType = this.eventType,
            status = EventInboxStatus.RECEIVED,
            limit = 100
        )

        records.forEach { record ->
            if (!inboxStore.tryAcquire(record.eventId)) {
                return@forEach
            }
            processOne(record)
        }

    }

    protected fun processOne(record: EventInbox) {
        try {
            val event = eventSerializer.deserialize(record.payload, eventClass.java)
            handle(event)
            inboxStore.markCompleted(record.eventId)
        } catch (exception: Exception) {

        }
    }

}