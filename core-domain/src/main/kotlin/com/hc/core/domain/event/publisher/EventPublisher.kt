package com.hc.core.domain.event.publisher

import com.hc.core.domain.event.Event

interface EventPublisher {

    fun <T: Event> publish(event: T)

    fun <T: Event> publishAll(events: Collection<T>) {
        events.forEach { publish(it) }
    }

}