package org.whiteprint.platform.core.messaging.outbox

interface EventOutboxStore {

    fun save(outbox: EventOutbox): EventOutbox

    fun claimPending(limit: Int): List<EventOutbox>

    fun markPublished(eventId: Long)

    fun markFailed(eventId: Long)

    fun resetStaleProcessing(olderThan: java.time.Instant): Int

}