package org.whiteprint.platform.infra.serializer.jackson

import org.whiteprint.platform.core.kernel.serializer.Serializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class JacksonSerializer: Serializer {

    val jsonMapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    override fun <T : Any> serializeToJson(obj: T): String {
        return jsonMapper.writeValueAsString(obj)
    }

    override fun <T : Any> deserializeFromJson(json: String, type: Class<T>): T {
        return jsonMapper.readValue(json, type)
    }

    override fun <T : Any> serializeToBytes(obj: T): ByteArray {
        return jsonMapper.writeValueAsBytes(obj)
    }

    override fun <T : Any> deserializeFromBytes(bytes: ByteArray, type: Class<T>): T {
        return jsonMapper.readValue(bytes, type)
    }
}