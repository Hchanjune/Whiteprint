package com.hc.core.messaging.policy

import com.hc.core.messaging.model.EventOutbox

interface EventEnveloper {

    fun <E: EventOutbox> envelope(eventOutbox: E): EventEnvelope<E>

}