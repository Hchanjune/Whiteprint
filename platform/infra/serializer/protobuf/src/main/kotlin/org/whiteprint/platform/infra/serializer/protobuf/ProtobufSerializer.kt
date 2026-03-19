package org.whiteprint.platform.infra.serializer.protobuf

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.whiteprint.platform.core.kernel.serializer.MessageSerializer
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class ProtobufSerializer : MessageSerializer {

    private val printer: JsonFormat.Printer = JsonFormat.printer()
    .alwaysPrintFieldsWithNoPresence()
    .preservingProtoFieldNames()
    .omittingInsignificantWhitespace()

    private val parser: JsonFormat.Parser = JsonFormat.parser()
        .ignoringUnknownFields()

    private val methodCache = ConcurrentHashMap<Class<*>, Method>()

    override fun <T : Any> serializeToMessage(obj: T): Message {
        return if (obj is Message) {
            obj
        } else {
            throw IllegalArgumentException("Object must be an instance of com.google.protobuf.Message")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Message> deserializeFromMessage(message: Message, type: Class<T>): T {
        if (type.isInstance(message)) return message as T
        return deserializeFromBytes(message.toByteArray(), type)
    }

    override fun <T : Any> serializeToBytes(obj: T): ByteArray {
        return if (obj is Message) {
            obj.toByteArray()
        } else {
            throw IllegalArgumentException("Only Protobuf Message can be serialized to bytes in this serializer")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Message> deserializeFromBytes(bytes: ByteArray, type: Class<T>): T {
        return try {
            val method = methodCache.computeIfAbsent(type) {
                it.getMethod("parseFrom", ByteArray::class.java)
            }
            method.invoke(null, bytes) as T
        } catch (e: Exception) {
            throw RuntimeException("Failed to deserialize Protobuf bytes to ${type.name}", e)
        }
    }

    override fun serializeToProtoJson(message: Message): String {
        return printer.print(message)
    }
}