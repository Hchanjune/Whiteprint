package org.whiteprint.platform.core.messaging.contract

import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.outbox.EventOutbox

interface EventEnveloper {

    fun envelope(outbox: EventOutbox): EventEnvelope

}