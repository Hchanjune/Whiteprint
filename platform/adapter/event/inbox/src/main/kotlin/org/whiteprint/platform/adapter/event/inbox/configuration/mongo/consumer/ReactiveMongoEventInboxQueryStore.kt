package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
import org.whiteprint.platform.core.messaging.inbox.EventInboxQueryStore
import org.whiteprint.platform.core.messaging.inbox.EventProcessingStatus

class ReactiveMongoEventInboxQueryStore(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : EventInboxQueryStore {

    override fun findByTraceId(traceId: String): List<EventProcessingStatus> =
        reactiveMongoTemplate.find(
            Query(Criteria.where("trace_id").`is`(traceId)),
            EventInboxDocument::class.java,
        ).collectList().block()?.map { it.toStatus() } ?: emptyList()

    override fun findByCausationId(causationId: String): List<EventProcessingStatus> =
        reactiveMongoTemplate.find(
            Query(Criteria.where("causation_id").`is`(causationId)),
            EventInboxDocument::class.java,
        ).collectList().block()?.map { it.toStatus() } ?: emptyList()

    private fun EventInboxDocument.toStatus() = EventProcessingStatus(
        traceId = traceId,
        causationId = causationId,
        eventType = eventType,
        status = status,
        processedAt = processedAt,
        errorMessage = errorMessage,
    )
}
