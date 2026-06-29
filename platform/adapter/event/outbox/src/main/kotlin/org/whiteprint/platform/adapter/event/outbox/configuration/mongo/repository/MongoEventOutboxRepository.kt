package org.whiteprint.platform.adapter.event.outbox.configuration.mongo.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.document.EventOutboxDocument

@Repository
interface MongoEventOutboxRepository : MongoRepository<EventOutboxDocument, Long>
