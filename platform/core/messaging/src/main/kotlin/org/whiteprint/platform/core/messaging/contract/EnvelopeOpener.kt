package org.whiteprint.platform.core.messaging.contract

import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.model.EventEnvelope

interface EnvelopeOpener {
    fun open(envelope: EventEnvelope): EventInbox
}