package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.entity.EventOutboxEntity
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.model.event.EventScope
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import org.whiteprint.platform.core.messaging.model.event.PartitionedEvent
import org.whiteprint.platform.core.messaging.model.event.external.ExternalEvent
import org.whiteprint.platform.core.messaging.model.event.internal.InternalEvent
import org.whiteprint.platform.core.messaging.outbox.EventContextProvider
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.outbox.EventPublisher
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import java.time.Instant

open class OutboxEventPublisher(
    private val outboxStore: EventOutboxStore,
    private val eventContextProvider: EventContextProvider,
    private val eventSerializer: EventSerializer,
    private val topicResolver: TopicResolver
): EventPublisher {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun publish(event: Event) {
        val eventContext = eventContextProvider.current()

        val eventScope = when (event) {
            is InternalEvent -> EventScope.INTERNAL
            is ExternalEvent -> EventScope.EXTERNAL
            else -> throw EventException(EventPolicy.EVENT_SCOPE_NOT_DEFINED)
        }

        val partitionKey = when (event) {
            is PartitionedEvent -> event.partitionKey()
            else -> 0L
        }

        val entity = EventOutboxEntity(
            eventId = TsidGenerator.generate(),
            traceId = eventContext.traceId,
            causationId = eventContext.causationId,
            occurredAt = Instant.now(),
            issuer = eventContext.issuer,
            producer = topicResolver.resolve(),
            schemaVersion = event.schemaVersion,
            partitionKey = partitionKey,
            eventScope = eventScope,
            eventType = event.eventType,
            payload = eventSerializer.toByteArray(event),
            payloadJson = eventSerializer.toJson(event),
            metadataJson = eventSerializer.metadataToJson(eventContext.metadata),
            status = EventOutboxStatus.PENDING,
        )

        outboxStore.save(entity)
    }
}