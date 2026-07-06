package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument

@Repository
interface MongoEventInboxRepository : MongoRepository<EventInboxDocument, Long> {
    fun findAllByTraceId(traceId: String): List<EventInboxDocument>
    fun findAllByCausationId(causationId: String): List<EventInboxDocument>
}
