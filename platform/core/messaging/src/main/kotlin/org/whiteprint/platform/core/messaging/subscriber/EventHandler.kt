package org.whiteprint.platform.core.messaging.subscriber

import org.whiteprint.platform.core.messaging.model.Event

interface EventHandler<E: Event> {
    fun handle(event: E)
}