package com.hc.core.messaging.provider

import com.hc.core.messaging.model.Event

interface EventRecorder {
    val events: List<Event>
    fun record(event: Event)
    fun pullEvents(): List<Event>
}