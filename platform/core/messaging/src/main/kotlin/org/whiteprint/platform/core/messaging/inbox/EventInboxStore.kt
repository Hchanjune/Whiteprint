package org.whiteprint.platform.core.messaging.inbox

interface EventInboxStore {
    fun save(inbox: EventInbox): EventInbox
    fun tryAcquire(eventId: Long): Boolean
    fun markCompleted(eventId: Long)
    fun markFailed(eventId: Long, error: String)
    fun markDead(eventId: Long)
    fun findById(eventId: Long): EventInbox?
    fun findAllByEventTypeAndStatus(eventType: String, status: EventInboxStatus, limit: Int): List<EventInbox>
    fun resetStaleProcessing(eventType: String, olderThan: java.time.Instant): Int
}