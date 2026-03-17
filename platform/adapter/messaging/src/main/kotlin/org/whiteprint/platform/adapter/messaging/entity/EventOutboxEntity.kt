package org.whiteprint.platform.adapter.messaging.entity

import org.whiteprint.platform.core.messaging.model.EventOutbox
import org.whiteprint.platform.core.messaging.model.EventStatus
import org.whiteprint.platform.core.messaging.model.EventType
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
    @Column(name = "event_type")
    override val eventType: EventType,

    @Column(name = "event_name")
    override val eventName: String,

    @Column(name = "event")
    override val event: String,

    @Column(name = "metadata")
    override val metadata: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    override var status: EventStatus
): EventOutbox