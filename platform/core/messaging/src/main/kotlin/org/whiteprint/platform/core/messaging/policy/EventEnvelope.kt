package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.model.EventType
import java.time.Instant

interface EventEnvelope<E: org.whiteprint.platform.core.messaging.model.Event> {
    val eventId: Long
    val traceId: String
    val causationId: String
    val occurredAt: Instant
    val issuer: String
    val producer: String
    val schemaVersion: String
    val partitionKey: Long
    val eventType: org.whiteprint.platform.core.messaging.model.EventType
    val eventName: String
        get() = this.event.name
    val event: E
    val metadata: Map<String, String>
}