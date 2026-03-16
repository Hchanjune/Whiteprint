package com.hc.core.messaging.policy

import com.hc.core.messaging.model.Event
import com.hc.core.messaging.model.EventType
import java.time.Instant

interface EventEnvelope<E: Event> {
    val eventId: String
    val traceId: String
    val causationId: String
    val occurredAt: Instant
    val issuer: String
    val producer: String
    val schemaVersion: String
    val partitionKey: String
    val eventType: EventType
    val eventName: String
        get() = this.event.name
    val event: E
    val metadata: Map<String, String>
}