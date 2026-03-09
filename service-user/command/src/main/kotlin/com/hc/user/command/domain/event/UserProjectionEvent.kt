package com.hc.user.command.domain.event

import com.hc.core.event.IntegrationEvent
import java.time.Instant

data class UserProjectionEvent(
    override val spanId: String,
    override val source: String,
    override val sequence: Long,
    override val schemaVersion: String,
    override val retryCount: Int,
    override val partitionKey: String,
    override val eventId: String,
    override val traceId: String,
    override val causationId: String,
    override val eventType: String,
    override val payload: Any,
    override val occurredAt: Instant
): IntegrationEvent