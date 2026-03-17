package com.hc.infra.jpa.messaging.service

import com.hc.core.messaging.policy.EventEnvelope
import com.hc.core.messaging.policy.EventEnveloper

class OutboxEventEnveloper: EventEnveloper {
    override fun <E : Event> envelope(event: E): EventEnvelope<E> {
        TODO("Not yet implemented")
    }
}