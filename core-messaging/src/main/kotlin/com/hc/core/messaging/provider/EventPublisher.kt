package com.hc.core.messaging.provider

import com.hc.core.messaging.model.Event

interface EventPublisher {

    fun publishAllFrom(recorder: EventRecorder) {
        this.publishAll(recorder.pullEvents())
    }

    fun <E: Event> publish(event: E)

    fun <E: Event> publishAll(events: Collection<E>) {
        events.forEach { publish(it) }
    }

}