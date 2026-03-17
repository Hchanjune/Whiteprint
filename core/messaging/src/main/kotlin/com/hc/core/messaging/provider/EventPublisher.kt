package com.hc.core.messaging.provider

import com.hc.core.messaging.model.Event

interface EventPublisher {

    fun publishAllFrom(recorder: EventRecorder) {
        this.publishAll(recorder.pullEvents())
    }

    fun <E: Event<Any>> publish(event: E)

    fun <E: Event<Any>> publishAll(events: Collection<E>) {
        events.forEach { publish(it) }
    }

}