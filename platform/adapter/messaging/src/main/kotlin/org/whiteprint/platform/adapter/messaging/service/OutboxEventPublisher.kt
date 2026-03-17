package org.whiteprint.platform.adapter.messaging.service

import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.model.EventStatus
import org.whiteprint.platform.core.messaging.model.EventType
import org.whiteprint.platform.core.messaging.model.external.ExternalEvent
import org.whiteprint.platform.core.messaging.model.internal.InternalEvent
import org.whiteprint.platform.core.messaging.policy.EventPayloadSerializer
import org.whiteprint.platform.core.messaging.policy.TopicResolver
import org.whiteprint.platform.core.messaging.provider.EventPublisher
import org.whiteprint.platform.adapter.messaging.entity.EventOutboxEntity
import org.whiteprint.platform.adapter.messaging.repository.EventOutboxRepository
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

open class OutboxEventPublisher(
    private val outboxRepository: EventOutboxRepository,
    private val eventSerializer: EventPayloadSerializer,
    private val topicResolver: TopicResolver
): EventPublisher {

    @Transactional
    override fun <E : Event<Any>> publish(event: E) {
        val eventType = when (event) {
            is InternalEvent<*> -> EventType.INTERNAL
            is ExternalEvent<*> -> EventType.EXTERNAL
            else -> EventType.ALL
        }

        val entity = EventOutboxEntity(
            eventId = TsidGenerator.generate(),
            traceId = Operations.context.traceId,
            causationId = Operations.context.causationId,
            occurredAt = Instant.now(),
            issuer = Operations.context.issuer,
            producer = topicResolver.resolve(),
            schemaVersion = event.schemaVersion,
            partitionKey = event.key,
            eventType = eventType,
            eventName = event.name,
            event = eventSerializer.serialize(event.payload),
            metadata = "",
            status = EventStatus.PENDING,
        )

        outboxRepository.save(entity)
    }
}