package org.whiteprint.platform.adapter.messaging.outbox.entity

import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.model.event.EventStatus
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
    override val eventId: Long,

    @Column(name = "trace_id")
    override val traceId: String,

    @Column(name = "causation_id")
    override val causationId: String,

    @Column(name = "occurred_at")
    override val occurredAt: Instant,

    @Column(name = "issuer")
    override val issuer: String,

    @Column(name = "producer")
    override val producer: String,

    @Column(name = "schema_version")
    override val schemaVersion: String,

    @Column(name = "partition_key")
    override val partitionKey: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_scope")
    override val eventScope: EventScope,

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
    override var status: EventStatus
): EventOutbox