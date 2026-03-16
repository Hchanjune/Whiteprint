package com.hc.core.messaging.policy

import com.hc.core.messaging.model.Event

interface EventEnveloper {

    fun <E: Event> envelope(event: E): EventEnvelope<E>

}