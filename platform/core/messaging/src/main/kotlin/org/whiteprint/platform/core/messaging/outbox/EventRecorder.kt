package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.Event

interface EventRecorder {
    val events: List<Event>
    fun record(event: Event)
    fun pullEvents(): List<Event>
}