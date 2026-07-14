package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.repository.MongoEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInboxQueryStore
import org.whiteprint.platform.core.messaging.inbox.EventProcessingStatus

@ManagedRepository
open class MongoEventInboxQueryStore(
    private val repository: MongoEventInboxRepository
) : EventInboxQueryStore {

    override fun findByTraceId(traceId: String): List<EventProcessingStatus> =
        repository.findAllByTraceId(traceId).map { it.toStatus() }

    override fun findByCausationId(causationId: String): List<EventProcessingStatus> =
        repository.findAllByCausationId(causationId).map { it.toStatus() }

    private fun EventInboxDocument.toStatus() = EventProcessingStatus(
        traceId = traceId,
        causationId = causationId,
        eventType = eventType,
        status = status,
        processedAt = processedAt,
        errorMessage = errorMessage,
    )
}
