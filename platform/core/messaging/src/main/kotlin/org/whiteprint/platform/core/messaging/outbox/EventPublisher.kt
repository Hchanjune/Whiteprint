package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.Event

interface EventPublisher {

    fun publishAllFrom(recorder: EventRecorder) {
        this.publishAll(recorder.pullEvents())
    }

    fun publish(event: Event)

    fun publishAll(events: Collection<Event>) {
        events.forEach { publish(it) }
    }

}