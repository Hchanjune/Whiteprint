package org.whiteprint.platform.core.messaging.inbox

import java.time.Instant

data class EventProcessingStatus(
    val traceId: String,
    val causationId: String?,
    val eventType: String,
    val status: EventInboxStatus,
    val processedAt: Instant?,
    val errorMessage: String?,
)
