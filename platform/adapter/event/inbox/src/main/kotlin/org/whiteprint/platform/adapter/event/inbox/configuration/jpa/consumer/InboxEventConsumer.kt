package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import io.github.hchanjune.omk.core.annotations.ManagedEventHandler
import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.model.EventEnvelope

open class InboxEventConsumer(
    private val inboxStore: EventInboxStore,
    private val envelopeOpener: EnvelopeOpener,
): EventConsumer {

    // EventEnvelope carries traceId/causationId/issuer/eventType as plain fields,
    // which the aspect extracts by reflection — the producer-side trace continues here.
    @ManagedEventHandler
    override fun consume(eventEnvelope: EventEnvelope) {
        val inbox = envelopeOpener.open(eventEnvelope)
        inboxStore.save(inbox)
    }

}