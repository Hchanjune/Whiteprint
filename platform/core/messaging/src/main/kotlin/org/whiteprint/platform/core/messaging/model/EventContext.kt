package org.whiteprint.platform.core.messaging.model

data class EventContext(
    val traceId: String,
    val causationId: String,
    val issuer: String,
    val metadata: Map<String, String>,
)