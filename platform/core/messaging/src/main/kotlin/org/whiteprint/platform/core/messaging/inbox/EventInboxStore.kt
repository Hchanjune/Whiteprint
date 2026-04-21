package org.whiteprint.platform.core.messaging.inbox

interface EventInboxStore {
    fun save(inbox: EventInbox): EventInbox
    fun tryAcquire(eventId: String): Boolean
    fun markCompleted(eventId: String)
    fun markFailed(eventId: String, error: String)
    fun markDead(eventId: String)
    fun findById(eventId: String): EventInbox?
}