package org.whiteprint.platform.core.messaging.outbox

import org.whiteprint.platform.core.messaging.model.Event

interface EventRecorder {
    /**
     * Records a event to be published later.
     * Implementations must maintain an internal mutable collection to store recorded events.
     */
    fun record(event: Event)
    /**
     * Returns all recorded events and clears the internal collection.
     */
    fun pullEvents(): List<Event>
}