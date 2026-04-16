package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.event.EventScope
import org.whiteprint.platform.core.messaging.model.event.EventStatus
import java.time.Instant

interface  EventOutbox {

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

    val status: EventStatus

    val attemptCount: Int

    val lastAttemptedAt: Instant?

}