package org.whiteprint.platform.core.messaging.model

import org.whiteprint.platform.core.messaging.model.event.EventScope
import java.time.Instant

@Suppress("ArrayInDataClass")
data class EventEnvelope (
    val eventId: Long,
    val traceId: String,
    val causationId: String,
    val occurredAt: Instant,
    val issuer: String,
    val producer: String,
    val schemaVersion: String,
    val partitionKey: Long,
    val eventScope: EventScope,
    val eventType: String,
    val payload: ByteArray,
    val metadata: String
)