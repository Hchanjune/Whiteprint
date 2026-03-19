package org.whiteprint.platform.core.kernel.serializer

interface Serializer {
    fun <T: Any> serializeToJson(obj: T): String
    fun <T: Any> deserializeFromJson(json: String, type: Class<T>): T
    fun <T: Any> serializeToBytes(obj: T): ByteArray
    fun <T: Any> deserializeFromBytes(bytes: ByteArray, type: Class<T>): T
}