package com.hc.core.messaging.policy

import com.hc.core.messaging.model.EventEnvelope
import com.hc.core.messaging.model.event.Event

interface EventSerializer<E: Event> {

    fun serialize(envelope: EventEnvelope<E>): String

    fun deserialize(data: String): EventEnvelope<E>

    fun deserializeEvent(event: Event): E

}