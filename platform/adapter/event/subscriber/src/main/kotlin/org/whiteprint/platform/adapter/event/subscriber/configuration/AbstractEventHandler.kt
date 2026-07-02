package org.whiteprint.platform.adapter.event.subscriber.configuration

import kotlinx.coroutines.runBlocking
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
import java.time.Instant

/**
 * Abstract base for transactional inbox event handlers.
 *
 * Polls the inbox store on a fixed schedule, acquires per-record locks to prevent
 * duplicate processing, and delegates to [handle] with the deserialized payload.
 * Subclasses only need to implement [handle].
 *
 * Observability context (traceId, causationId, etc.) is handled by the
 * [@ManagedEventHandler][io.github.hchanjune.omk.core.annotations.ManagedEventHandler]
 * AOP aspect — annotate [handle] and declare the relevant fields on your event class.
 */
abstract class AbstractEventHandler<E: Event>: EventHandler<E>, ApplicationContextAware {

    private lateinit var inboxStore: EventInboxStore
    private lateinit var eventSerializer: InboxEventSerializer
    private lateinit var applicationContext: ApplicationContext
    private var claimTimeoutMillis: Long = 300_000L

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        inboxStore = applicationContext.getBean<EventInboxStore>()
        eventSerializer = applicationContext.getBean<InboxEventSerializer>()
        claimTimeoutMillis = applicationContext.getBean<EventSubscriberAutoConfigurationProperties>().claimTimeoutMillis
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

    @Scheduled(fixedDelay = 60_000)
    open fun recoverStaleProcessing() {
        val threshold = Instant.now().minusMillis(claimTimeoutMillis)
        inboxStore.resetStaleProcessing(eventType, threshold)
    }

    protected fun processOne(record: EventInbox) {
        try {
            val event = eventSerializer.deserialize(record.payload, eventClass.java)
            runBlocking {
                applicationContext.getBean(this@AbstractEventHandler::class.java).handle(event)
            }
            inboxStore.markCompleted(record.eventId)
        } catch (exception: Exception) {
            inboxStore.markFailed(record.eventId, exception.message ?: "")
        }
    }

}