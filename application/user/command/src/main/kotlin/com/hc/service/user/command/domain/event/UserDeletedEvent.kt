package com.hc.service.user.command.domain.event

import com.hc.core.platform.event.DomainEvent
import java.time.Instant

data class UserDeletedEvent(
    override val aggregateId: String,
    override val aggregateType: String,
    override val eventId: String,
    override val traceId: String,
    override val causationId: String,
    override val eventType: String,
    override val payload: Any,
    override val occurredAt: Instant
): DomainEvent
