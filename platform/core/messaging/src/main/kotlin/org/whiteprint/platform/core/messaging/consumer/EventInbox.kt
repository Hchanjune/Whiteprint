package org.whiteprint.platform.core.messaging.consumer

interface EventInbox {

    fun exists(eventId: String): Boolean

    fun record(eventId: String)

}