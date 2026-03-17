package com.hc.core.messaging.model.external

import com.hc.core.messaging.model.Event

interface ExternalEvent<out T>: Event<T> {
    val version: String
}