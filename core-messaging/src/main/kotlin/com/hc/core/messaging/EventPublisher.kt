package com.hc.core.messaging

interface EventPublisher {

    fun <E: Event> publish(event: E)

    fun <E: Event> publishAll(events: Collection<E>) {
        events.forEach { publish(it) }
    }

}