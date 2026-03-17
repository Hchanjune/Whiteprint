package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.messaging.model.EventOutbox

interface EventEnveloper {

    fun <E: org.whiteprint.platform.core.messaging.model.EventOutbox> envelope(eventOutbox: E): org.whiteprint.platform.core.messaging.policy.EventEnvelope<E>

}