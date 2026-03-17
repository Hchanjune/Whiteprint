package com.hc.infra.jpa.messaging.service

import com.hc.core.kernel.identifier.TsidGenerator
import com.hc.core.messaging.model.Event
import com.hc.core.messaging.model.EventStatus
import com.hc.core.messaging.model.EventType
import com.hc.core.messaging.model.external.ExternalEvent
import com.hc.core.messaging.model.internal.InternalEvent
import com.hc.core.messaging.policy.EventPayloadSerializer
import com.hc.core.messaging.policy.TopicResolver
import com.hc.core.messaging.provider.EventPublisher
import com.hc.infra.jpa.messaging.entity.EventOutboxEntity
import com.hc.infra.jpa.messaging.repository.EventOutboxRepository
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