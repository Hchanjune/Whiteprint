package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.model.EventEnvelope

class InboxEventConsumer(
    private val inboxStore: EventInboxStore,
    private val envelopeOpener: EnvelopeOpener,
): EventConsumer {

    override fun consume(eventEnvelope: EventEnvelope) {
        val inbox = envelopeOpener.open(eventEnvelope)
        inboxStore.save(inbox)
    }

}