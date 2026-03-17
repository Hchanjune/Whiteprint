package org.whiteprint.platform.adapter.messaging.service

import org.whiteprint.platform.core.messaging.policy.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventEnveloper

class OutboxEventEnveloper: EventEnveloper {
    override fun <E : Event> envelope(event: E): EventEnvelope<E> {
        TODO("Not yet implemented")
    }
}