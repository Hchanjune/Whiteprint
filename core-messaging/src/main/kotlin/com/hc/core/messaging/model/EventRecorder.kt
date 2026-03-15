package com.hc.core.messaging.model

import com.hc.core.messaging.model.event.Event

interface EventRecorder {
    val events: List<Event>
    fun record(event: Event)
    fun pullEvents(): List<Event>
}