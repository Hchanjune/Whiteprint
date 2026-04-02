package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.context

import io.github.hchanjune.omk.webmvc.Operations
import org.whiteprint.platform.core.messaging.model.EventContext
import org.whiteprint.platform.core.messaging.outbox.EventContextProvider

class OutboxEventContextProvider: EventContextProvider {
    override fun current() = EventContext(
        traceId = Operations.context.traceId,
        causationId = Operations.context.causationId,
        issuer = Operations.context.issuer,
        metadata = emptyMap(),
    )
}