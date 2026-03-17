package org.whiteprint.platform.core.messaging.provider

import org.whiteprint.platform.core.messaging.policy.EventEnvelope

interface EventProducer {

    fun produce(envelope: org.whiteprint.platform.core.messaging.policy.EventEnvelope<*>)

}