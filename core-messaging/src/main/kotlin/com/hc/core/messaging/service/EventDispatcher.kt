package com.hc.core.messaging.service

import com.hc.core.messaging.model.EventEnvelope

interface EventDispatcher {
    fun dispatch(event: EventEnvelope<*>)
}