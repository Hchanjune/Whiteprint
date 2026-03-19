package org.whiteprint.platform.core.kernel.serializer

import com.google.protobuf.Message

interface MessageSerializer {
    fun <T : Any> serializeToMessage(obj: T): Message
    fun <T : Message> deserializeFromMessage(message: Message, type: Class<T>): T

    fun <T : Any> serializeToBytes(obj: T): ByteArray
    fun <T : Message> deserializeFromBytes(bytes: ByteArray, type: Class<T>): T

    fun serializeToProtoJson(message: Message): String
}