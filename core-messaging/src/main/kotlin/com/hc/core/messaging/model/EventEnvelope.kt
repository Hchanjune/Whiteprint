package com.hc.core.messaging.model

import com.hc.core.messaging.model.event.Event
import java.time.Instant

interface EventEnvelope<E: Event> {
    val eventId: String
    val correlationId: String
    val causationId: String
    val occurredAt: Instant
    val issuer: String
    val producer: String
    val schemaVersion: String
    val partitionKey: String
    val eventType: String
    val eventName: String
        get() = this.event.name
    val event: E
    val metadata: Map<String, String>
}