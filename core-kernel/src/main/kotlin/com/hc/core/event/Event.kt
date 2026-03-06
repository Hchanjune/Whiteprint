package com.hc.core.event

import java.time.Instant

interface Event {
    val eventId: String
    val traceId: String
    val causationId: String
    val eventType: String
    val payload: Any
    val occurredAt: Instant
}