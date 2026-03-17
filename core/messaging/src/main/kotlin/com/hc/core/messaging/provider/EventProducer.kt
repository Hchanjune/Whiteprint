package com.hc.core.messaging.provider

import com.hc.core.messaging.policy.EventEnvelope

interface EventProducer {

    fun produce(envelope: EventEnvelope<*>)

}