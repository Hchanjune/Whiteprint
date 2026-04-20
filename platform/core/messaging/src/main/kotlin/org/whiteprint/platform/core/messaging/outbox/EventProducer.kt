package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.Event

interface EventProducer {

    fun produceAllFrom(recorder: EventRecorder) {
        this.produceAll(recorder.pullEvents())
    }

    fun produce(event: Event)

    fun produceAll(events: Collection<Event>) {
        events.forEach { produce(it) }
    }

}