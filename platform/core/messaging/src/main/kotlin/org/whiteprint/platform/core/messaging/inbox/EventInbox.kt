package org.whiteprint.platform.core.messaging.inbox

import org.whiteprint.platform.core.messaging.model.event.EventScope
import java.time.Instant

interface EventInbox {
    val eventId: Long
    val traceId: String
    val causationId: String?
    val occurredAt: Instant
    val issuer: String
    val producer: String
    val schemaVersion: String
    val partitionKey: Long
    val eventScope: EventScope
    val eventType: String
    val payload: ByteArray
    val payloadJson: String
    val metadataJson: String
    val status: EventInboxStatus
    val receivedAt: Instant
    val processedAt: Instant?
    val attemptCount: Int
    val lastAttemptedAt: Instant?
    val errorMessage: String?
}