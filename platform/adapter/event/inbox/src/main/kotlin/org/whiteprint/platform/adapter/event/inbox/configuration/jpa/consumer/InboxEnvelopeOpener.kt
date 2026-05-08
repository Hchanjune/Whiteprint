package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import java.time.Instant

class InboxEnvelopeOpener(
    private val eventSerializer: InboxEventSerializer,
): EnvelopeOpener {

    override fun open(envelope: EventEnvelope): EventInbox {
        val now = Instant.now()
        return EventInboxEntity(
            eventId = envelope.eventId,
            traceId = envelope.traceId,
            causationId = envelope.causationId,
            occurredAt = envelope.occurredAt,
            issuer = envelope.issuer,
            producer = envelope.producer,
            schemaVersion = envelope.schemaVersion,
            partitionKey = envelope.partitionKey,
            eventScope = envelope.eventScope,
            eventType = envelope.eventType,
            payload = envelope.payload,
            payloadJson = eventSerializer.payloadToJson(envelope.payload),
            metadataJson = envelope.metadata,
            status = EventInboxStatus.RECEIVED,
            receivedAt = now,
            processedAt = null,
            attemptCount = 0,
            lastAttemptedAt = null,
            errorMessage = null
        )
    }

}