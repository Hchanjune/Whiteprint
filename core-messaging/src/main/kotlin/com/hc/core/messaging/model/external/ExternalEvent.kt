package com.hc.core.messaging.model.external

import com.hc.core.messaging.model.Event

interface ExternalEvent: Event {
    val version: String
}