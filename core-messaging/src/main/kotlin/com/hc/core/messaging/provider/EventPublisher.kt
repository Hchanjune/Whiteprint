package com.hc.core.messaging.provider

import com.hc.core.messaging.model.EventRecorder
import com.hc.core.messaging.model.event.Event

interface EventPublisher {

    fun publishAllFrom(provider: EventRecorder) {
        this.publishAll(provider.pullEvents())
    }

    fun <E: Event> publish(event: E)

    fun <E: Event> publishAll(events: Collection<E>) {
        events.forEach { publish(it) }
    }

}