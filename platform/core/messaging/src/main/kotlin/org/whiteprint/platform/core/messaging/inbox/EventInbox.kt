package org.whiteprint.platform.core.messaging.inbox

interface EventInbox {

    fun exists(eventId: String): Boolean

    fun record(eventId: String)

}