package com.hc.core.messaging

import java.time.Instant

interface EventEnvelope<T: Event> {
    val eventId: String
    val causationId: String
    val eventType: String
    val occurredAt: Instant
    val producer: String
    val event: Event
    val schemaVersion: String
    val partitionKey: String
}