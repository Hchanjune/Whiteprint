package com.hc.core.messaging.policy

import com.hc.core.messaging.model.Event

inline fun <reified E: Event> EventSerializer.deserialize(raw: String): E {
    return this.deserializeRaw(raw, E::class.java)
}