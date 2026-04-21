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
    override val eventId: Long,

    @Column(name = "trace_id")
    override val traceId: String,

    @Column(name = "causation_id", nullable = true)
    override val causationId: String?,

    @Column(name = "occurred_at")
    override val occurredAt: Instant,

    @Column(name = "issuer")
    override val issuer: String,

    @Column(name = "producer")
    override val producer: String,

    @Column(name = "schema_version")
    override val schemaVersion: String,

    @Column(name = "partition_key")
    override var partitionKey: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_scope")
    override var eventScope: EventScope,

    @Column(name = "event_type")
    override val eventType: String,

    @Column(name = "payload")
    override val payload: ByteArray,

    @Column(name = "payload_json")
    override val payloadJson: String,

    @Column(name = "metadata_json")
    override val metadataJson: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    override val status: EventInboxStatus,

    @Column(name = "received_at", nullable = true)
    override val receivedAt: Instant,

    @Column(name = "processed_at", nullable = true)
    override val processedAt: Instant?,

    @Column(name = "attempt_count")
    override val attemptCount: Int,

    @Column(name = "last_attempted_at", nullable = true)
    override val lastAttemptedAt: Instant?,

    @Column(name = "error_message", nullable = true)
    override val errorMessage: String?
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