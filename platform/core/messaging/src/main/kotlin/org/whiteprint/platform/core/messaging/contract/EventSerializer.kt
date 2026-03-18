package org.whiteprint.platform.core.messaging.contract

import org.whiteprint.platform.core.messaging.model.Event

interface EventSerializer {

    fun toByteArray(event: Event): ByteArray

    fun toJson(event: Event): String

    fun metadataToJson(metadata: Map<String, String>): String

    fun<T: Event> deserialize(bytes: ByteArray, type: Class<T>): T

}