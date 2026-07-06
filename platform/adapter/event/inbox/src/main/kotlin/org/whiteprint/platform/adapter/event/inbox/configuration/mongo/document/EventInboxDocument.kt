package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.model.event.EventScope
import java.time.Instant

@Document(collection = "event_inbox")
@CompoundIndex(def = "{'event_type': 1, 'status': 1}")
data class EventInboxDocument(
    @Id
    @Field("_id")
    override var eventId: Long,

    @Indexed
    @Field("trace_id")
    override var traceId: String,

    @Indexed
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
    override var status: EventInboxStatus,

    @Field("received_at")
    override var receivedAt: Instant,

    @Field("processed_at")
    override var processedAt: Instant?,

    @Field("attempt_count")
    override var attemptCount: Int,

    @Field("last_attempted_at")
    override var lastAttemptedAt: Instant?,

    @Field("error_message")
    override var errorMessage: String?,
) : EventInbox {

    companion object {
        fun from(inbox: EventInbox) = EventInboxDocument(
            eventId = inbox.eventId,
            traceId = inbox.traceId,
            causationId = inbox.causationId,
            occurredAt = inbox.occurredAt,
            issuer = inbox.issuer,
            producer = inbox.producer,
            schemaVersion = inbox.schemaVersion,
            partitionKey = inbox.partitionKey,
            eventScope = inbox.eventScope,
            eventType = inbox.eventType,
            payload = inbox.payload,
            payloadJson = inbox.payloadJson,
            metadataJson = inbox.metadataJson,
            status = inbox.status,
            receivedAt = inbox.receivedAt,
            processedAt = inbox.processedAt,
            attemptCount = inbox.attemptCount,
            lastAttemptedAt = inbox.lastAttemptedAt,
            errorMessage = inbox.errorMessage,
        )
    }

}
