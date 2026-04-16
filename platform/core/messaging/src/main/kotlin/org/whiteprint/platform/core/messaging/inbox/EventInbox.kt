package org.whiteprint.platform.core.messaging.inbox

interface EventInbox {
    fun tryAcquire(eventId: String): Boolean
}