package org.whiteprint.platform.adapter.serializer.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.whiteprint.platform.core.kernel.serializer.MessageSerializer
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import org.whiteprint.platform.infra.serializer.protobuf.ProtobufSerializer

@Configuration
class SerializerConfiguration {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    @ConditionalOnMissingBean(MessageSerializer::class)
    fun messageSerializer(): MessageSerializer = ProtobufSerializer()


}