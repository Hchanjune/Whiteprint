package com.hc.core.messaging.provider

import com.hc.core.messaging.model.EventEnvelope

interface EventOutbox {

    fun append(event: EventEnvelope<*>)

    fun pullUnpublished(limit: Int): List<EventEnvelope<*>>

    fun markPublished(eventId: String)

}