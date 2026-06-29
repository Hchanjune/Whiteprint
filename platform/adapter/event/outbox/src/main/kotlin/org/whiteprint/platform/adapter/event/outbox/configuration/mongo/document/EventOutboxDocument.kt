package org.whiteprint.platform.adapter.event.outbox.configuration.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.whiteprint.platform.core.messaging.model.event.EventScope
import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import java.time.Instant

@Document(collection = "event_outbox")
data class EventOutboxDocument(
    @Id
    @Field("_id")
    override var eventId: Long,

    @Field("trace_id")
    override var traceId: String,

    @Field("causation_id")
    override var causationId: String?,

    @Field("occurred_at")
    override var occurredAt: Instant,

    @Field("issuer")
    override var issuer: String,

    @Field("producer")
    override var producer: String,

    @Field("schema_version")
    override var schemaVersion: String,

    @Field("partition_key")
    override var partitionKey: Long,

    @Field("event_scope")
    override var eventScope: EventScope,

    @Field("event_type")
    override var eventType: String,

    @Field("payload")
    override var payload: ByteArray,

    @Field("payload_json")
    override var payloadJson: String,

    @Field("metadata_json")
    override var metadataJson: String,

    @Indexed
    @Field("status")
    override var status: EventOutboxStatus,

    @Field("attempt_count")
    override var attemptCount: Int = 0,

    @Field("last_attempted_at")
    override var lastAttemptedAt: Instant? = null,
) : EventOutbox {

    companion object {
        fun from(outbox: EventOutbox) = EventOutboxDocument(
            eventId = outbox.eventId,
            traceId = outbox.traceId,
            causationId = outbox.causationId,
            occurredAt = outbox.occurredAt,
            issuer = outbox.issuer,
            producer = outbox.producer,
            schemaVersion = outbox.schemaVersion,
            partitionKey = outbox.partitionKey,
            eventScope = outbox.eventScope,
            eventType = outbox.eventType,
            payload = outbox.payload,
            payloadJson = outbox.payloadJson,
            metadataJson = outbox.metadataJson,
            status = outbox.status,
            attemptCount = outbox.attemptCount,
            lastAttemptedAt = outbox.lastAttemptedAt,
        )
    }

}
