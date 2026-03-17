package com.hc.core.messaging.policy

import com.hc.core.messaging.model.Event

inline fun <reified T: Any> EventPayloadSerializer.deserialize(raw: String): T {
    return this.deserializeRaw(raw, T::class.java)
}