package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher

import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.outbox.EventOutbox

class OutboxEventEnveloper: EventEnveloper {

    override fun envelope(outbox: EventOutbox): EventEnvelope {
        return EventEnvelope(
            eventId = outbox.eventId,
            traceId = outbox.traceId,
            causationId = outbox.causationId,
            occurredAt = outbox.occurredAt,
            issuer = outbox.issuer,
            producer = outbox.producer,
            schemaVersion = outbox.schemaVersion,
            partitionKey = outbox.partitionKey,
            eventScope = outbox.eventScope,
            eventType = outbox.eventType,
            payload = outbox.payload,
            metadata = outbox.metadataJson
        )
    }

}