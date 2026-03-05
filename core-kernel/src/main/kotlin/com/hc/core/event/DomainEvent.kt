package com.hc.core.event

import java.time.Instant

interface DomainEvent: Event {
    val eventId: String
    val correlationId: String
    val occurredAt: Instant
    val aggregateId: String
    val aggregateType: String
    val eventType: String
}