package org.whiteprint.platform.core.messaging.model

import java.time.Instant

interface  EventOutbox {

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

    val event: String

    val metadata: String

    var status: org.whiteprint.platform.core.messaging.model.EventStatus

}