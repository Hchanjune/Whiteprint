package org.whiteprint.platform.core.messaging.model

import org.whiteprint.platform.core.messaging.model.event.EventScope
import java.time.Instant

data class EventEnvelope (
    val eventId: Long,
    val traceId: String,
    val causationId: String?,
    val occurredAt: Instant,
    val issuer: String,
    val producer: String,
    val schemaVersion: String,
    val partitionKey: Long,
    val eventScope: EventScope,
    val eventType: String,
    val payload: ByteArray,
    val metadata: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventEnvelope

        if (eventId != other.eventId) return false
        if (partitionKey != other.partitionKey) return false
        if (traceId != other.traceId) return false
        if (causationId != other.causationId) return false
        if (occurredAt != other.occurredAt) return false
        if (issuer != other.issuer) return false
        if (producer != other.producer) return false
        if (schemaVersion != other.schemaVersion) return false
        if (eventScope != other.eventScope) return false
        if (eventType != other.eventType) return false
        if (!payload.contentEquals(other.payload)) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventId.hashCode()
        result = 31 * result + partitionKey.hashCode()
        result = 31 * result + traceId.hashCode()
        result = 31 * result + causationId.hashCode()
        result = 31 * result + occurredAt.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + producer.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + eventScope.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }
}