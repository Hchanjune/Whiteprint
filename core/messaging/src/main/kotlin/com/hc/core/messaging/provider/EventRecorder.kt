package com.hc.core.messaging.provider

import com.hc.core.messaging.model.Event

interface EventRecorder {
    val events: List<Event<Any>>
    fun record(event: Event<Any>)
    fun pullEvents(): List<Event<Any>>
}