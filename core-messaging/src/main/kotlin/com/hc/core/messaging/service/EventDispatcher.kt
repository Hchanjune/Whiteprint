package com.hc.core.messaging.service

import com.hc.core.messaging.policy.EventEnvelope

interface EventDispatcher {
    fun dispatch(event: EventEnvelope<*>)
}