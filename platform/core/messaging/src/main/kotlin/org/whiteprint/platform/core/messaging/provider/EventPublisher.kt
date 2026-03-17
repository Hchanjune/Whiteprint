package org.whiteprint.platform.core.messaging.provider

import org.whiteprint.platform.core.messaging.model.Event

interface EventPublisher {

    fun publishAllFrom(recorder: org.whiteprint.platform.core.messaging.provider.EventRecorder) {
        this.publishAll(recorder.pullEvents())
    }

    fun <E: org.whiteprint.platform.core.messaging.model.Event<Any>> publish(event: E)

    fun <E: org.whiteprint.platform.core.messaging.model.Event<Any>> publishAll(events: Collection<E>) {
        events.forEach { publish(it) }
    }

}