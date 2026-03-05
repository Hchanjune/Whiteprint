package com.hc.core.event.publisher

import com.hc.core.event.Event

interface EventPublisher {

    fun <T: Event> publish(event: T)

    fun <T: Event> publishAll(events: Collection<T>) {
        events.forEach { publish(it) }
    }

}