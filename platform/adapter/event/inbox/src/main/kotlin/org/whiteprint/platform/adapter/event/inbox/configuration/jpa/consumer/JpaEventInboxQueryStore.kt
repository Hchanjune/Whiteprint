package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository.JpaEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInboxQueryStore
import org.whiteprint.platform.core.messaging.inbox.EventProcessingStatus

@ManagedRepository
open class JpaEventInboxQueryStore(
    private val repository: JpaEventInboxRepository
) : EventInboxQueryStore {

    override fun findByTraceId(traceId: String): List<EventProcessingStatus> =
        repository.findAllByTraceId(traceId).map { it.toStatus() }

    override fun findByCausationId(causationId: String): List<EventProcessingStatus> =
        repository.findAllByCausationId(causationId).map { it.toStatus() }

    private fun EventInboxEntity.toStatus() = EventProcessingStatus(
        traceId = traceId,
        causationId = causationId,
        eventType = eventType,
        status = status,
        processedAt = processedAt,
        errorMessage = errorMessage,
    )
}
