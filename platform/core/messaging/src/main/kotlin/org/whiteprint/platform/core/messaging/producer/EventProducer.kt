package org.whiteprint.platform.core.messaging.producer

import org.whiteprint.platform.core.messaging.model.EventEnvelope

interface EventProducer {

    fun produce(envelope: EventEnvelope)

}