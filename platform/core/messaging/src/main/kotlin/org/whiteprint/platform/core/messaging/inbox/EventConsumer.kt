package org.whiteprint.platform.core.messaging.inbox

import org.whiteprint.platform.core.messaging.model.EventEnvelope

interface EventConsumer {
    fun consume(eventEnvelope: EventEnvelope)
}