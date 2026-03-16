package com.hc.core.kernel.serializer

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object JsonSerializer {
    val default: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()
}