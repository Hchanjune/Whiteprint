package org.whiteprint.platform.adapter.serializer.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(SerializerConfiguration::class)
class SerializerAutoConfiguration