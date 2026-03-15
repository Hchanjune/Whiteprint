package com.hc.core.messaging.service

import com.hc.core.messaging.model.event.Event

interface EventHandler<E: Event> {
    fun handle(event: E)
}