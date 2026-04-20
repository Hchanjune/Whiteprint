package org.whiteprint.platform.core.messaging.publisher

import org.whiteprint.platform.core.messaging.model.EventEnvelope

interface EventPublisher {

    fun publish(envelope: EventEnvelope)

}