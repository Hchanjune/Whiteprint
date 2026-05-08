package org.whiteprint.platform.adapter.event.subscriber.configuration

import io.github.hchanjune.omk.core.event.EventMetadata
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.subscriber.EventHandler

/**
 * Abstract base for transactional inbox event handlers.
 *
 * Polls the inbox store on a fixed schedule, acquires per-record locks to prevent
 * duplicate processing, and delegates to [handle] with the deserialized payload.
 *
 * Distributed trace context (traceId, causationId, issuer, eventType) is automatically
 * initialized via Operation Manager Kit before each [handle] invocation, using metadata
 * stored in the inbox record. Subclasses only need to implement [handle].
 */
abstract class AbstractEventHandler<E: Event>: EventHandler<E>, ApplicationContextAware {

    private lateinit var inboxStore: EventInboxStore
    private lateinit var eventSerializer: InboxEventSerializer
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        inboxStore = applicationContext.getBean<EventInboxStore>()
        eventSerializer = applicationContext.getBean<InboxEventSerializer>()
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

    /**
     * Self Invocation Problem + Observability Manual Config (InBox/OutBox Pattern)
     */
    protected fun processOne(record: EventInbox) {
        Operations.initializeForEvent(
            EventMetadata(
                traceId = record.traceId,
                causationId = record.causationId,
                issuer = record.issuer,
                eventType = record.eventType,
            )
        )
        val context = Operations.context
        try {
            val event = eventSerializer.deserialize(record.payload, eventClass.java)
            applicationContext.getBean(this::class.java).handle(event)
            Operations.complete()
            Operations.hook?.onSuccess(context)
            inboxStore.markCompleted(record.eventId)
        } catch (exception: Exception) {
            Operations.complete()
            Operations.hook?.onFailure(context, exception)
            inboxStore.markFailed(record.eventId, exception.message ?: "")
        } finally {
            Operations.clear()
        }
    }

}