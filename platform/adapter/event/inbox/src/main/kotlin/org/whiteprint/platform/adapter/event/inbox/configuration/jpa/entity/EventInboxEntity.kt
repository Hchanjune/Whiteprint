package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.model.event.EventScope
import java.time.Instant

@Entity
@Table(name = "event_inbox")
class EventInboxEntity(
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
    override var status: EventInboxStatus,

    @Column(name = "received_at", nullable = true)
    override var receivedAt: Instant,

    @Column(name = "processed_at", nullable = true)
    override var processedAt: Instant?,

    @Column(name = "attempt_count")
    override var attemptCount: Int,

    @Column(name = "last_attempted_at", nullable = true)
    override var lastAttemptedAt: Instant?,

    @Column(name = "error_message", nullable = true)
    override var errorMessage: String?
): EventInbox {

    companion object {
        fun from(eventInbox: EventInbox): EventInboxEntity {
            return EventInboxEntity(
                eventId = eventInbox.eventId,
                traceId = eventInbox.traceId,
                causationId = eventInbox.causationId,
                occurredAt = eventInbox.occurredAt,
                issuer = eventInbox.issuer,
                producer = eventInbox.producer,
                schemaVersion = eventInbox.schemaVersion,
                partitionKey = eventInbox.partitionKey,
                eventScope = eventInbox.eventScope,
                eventType = eventInbox.eventType,
                payload = eventInbox.payload,
                payloadJson = eventInbox.payloadJson,
                metadataJson = eventInbox.metadataJson,
                status = eventInbox.status,
                receivedAt = eventInbox.receivedAt,
                processedAt = eventInbox.processedAt,
                attemptCount = eventInbox.attemptCount,
                lastAttemptedAt = eventInbox.lastAttemptedAt,
                errorMessage = eventInbox.errorMessage
            )
        }
    }

}