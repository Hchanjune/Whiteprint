package org.whiteprint.platform.core.messaging.policy

inline fun <reified T: Any> EventPayloadSerializer.deserialize(raw: String): T {
    return this.deserializeRaw(raw, T::class.java)
}