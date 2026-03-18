package org.whiteprint.platform.core.messaging.contract

import org.whiteprint.platform.core.messaging.model.Event

inline fun <reified T: Event> EventSerializer.deserialize(bytes: ByteArray): T {
    return this.deserialize(bytes, T::class.java)
}