package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.messaging.model.Event

inline fun <reified T: Any> org.whiteprint.platform.core.messaging.policy.EventPayloadSerializer.deserialize(raw: String): T {
    return this.deserializeRaw(raw, T::class.java)
}