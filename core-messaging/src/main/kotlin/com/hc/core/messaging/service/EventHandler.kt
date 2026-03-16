package com.hc.core.messaging.service

import com.hc.core.messaging.model.Event

interface EventHandler<E: Event> {
    fun handle(event: E)
}