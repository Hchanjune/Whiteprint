package org.whiteprint.platform.core.messaging.provider

import org.whiteprint.platform.core.messaging.model.Event

interface EventRecorder {
    val events: List<Event<Any>>
    fun record(event: Event<Any>)
    fun pullEvents(): List<Event<Any>>
}