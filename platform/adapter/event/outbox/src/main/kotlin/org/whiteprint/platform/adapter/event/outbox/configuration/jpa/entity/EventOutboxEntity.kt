package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.entity

import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import org.whiteprint.platform.core.messaging.model.event.EventScope
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "event_outbox")
class EventOutboxEntity(
    @Id
    @Column(name = "event_id")
    override var eventId: Long,

    @Column(name = "trace_id")
    override var traceId: String,

    @Column(name = "causation_id", nullable = true)
    override var causationId: String?,

    @Column(name = "occurred_at")
    override var occurredAt: Instant,

    @Column(name = "issuer")
    override var issuer: String,

    @Column(name = "producer")
    override var producer: String,

    @Column(name = "schema_version")
    override var schemaVersion: String,

    @Column(name = "partition_key")
    override var partitionKey: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_scope")
    override var eventScope: EventScope,

    @Column(name = "event_type")
    override var eventType: String,

    @Column(name = "payload")
    override var payload: ByteArray,

    @Column(name = "payload_json")
    override var payloadJson: String,

    @Column(name = "metadata_json")
    override var metadataJson: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    override var status: EventOutboxStatus,

    @Column(name = "attempt_count")
    override var attemptCount: Int = 0,

    @Column(name = "last_attempted_at", nullable = true)
    override var lastAttemptedAt: Instant? = null
): EventOutbox {

    companion object {
        fun from(eventOutbox: EventOutbox): EventOutboxEntity {
            return EventOutboxEntity(
                eventId = eventOutbox.eventId,
                traceId = eventOutbox.traceId,
                causationId = eventOutbox.causationId,
                occurredAt = eventOutbox.occurredAt,
                issuer = eventOutbox.issuer,
                producer = eventOutbox.producer,
                schemaVersion = eventOutbox.schemaVersion,
                partitionKey = eventOutbox.partitionKey,
                eventScope = eventOutbox.eventScope,
                eventType = eventOutbox.eventType,
                payload = eventOutbox.payload,
                payloadJson = eventOutbox.payloadJson,
                metadataJson = eventOutbox.metadataJson,
                status = eventOutbox.status,
                attemptCount = eventOutbox.attemptCount,
                lastAttemptedAt = eventOutbox.lastAttemptedAt,
            )
        }
    }

}