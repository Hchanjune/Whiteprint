package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.messaging.model.EventOutbox

interface EventEnveloper {

    fun <E: EventOutbox> envelope(eventOutbox: E): EventEnvelope<E>

}