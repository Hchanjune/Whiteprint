package org.whiteprint.platform.core.messaging.policy

interface EventPayloadSerializer {

    fun serialize(payload: Any): String

    fun<T: Any> deserializeRaw(raw: String, type: Class<T>): T

}