package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.EventContext

interface EventContextProvider {
    fun current(): EventContext
}