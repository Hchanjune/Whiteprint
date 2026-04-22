package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.subscriber.EventHandler

abstract class AbstractEventHandler<E: Event>: EventHandler<E>, ApplicationContextAware {

    private lateinit var inboxStore: EventInboxStore
    private lateinit var eventSerializer: EventSerializer

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        inboxStore = applicationContext.getBean<EventInboxStore>()
        eventSerializer = applicationContext.getBean<EventSerializer>()
    }

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
            inboxStore.markFailed(record.eventId, exception.message ?: "")
        }
    }

}