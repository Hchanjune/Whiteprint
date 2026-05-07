package org.whiteprint.platform.adapter.event.outbox.configuration.jpa

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.context.OutboxEventContextProvider
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.producer.JpaEventOutboxStore
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.producer.OutboxEventEnveloper
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.producer.OutboxEventProducer
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.repository.JpaEventOutboxRepository
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer.OutboxEventSerializerImpl
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.outbox.EventContextProvider
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.outbox.EventProducer
import org.whiteprint.platform.core.messaging.outbox.OutboxEventSerializer

@Configuration
@ConditionalOnProperty(
    prefix = "adapter.event.outbox",
    name = ["infrastructure-implementation"],
    havingValue = "jpa",
    matchIfMissing = false
)
@EnableJpaRepositories(basePackageClasses = [JpaEventOutboxRepository::class])
class JpaEventOutboxConfiguration {

    @Bean
    fun eventEnveloper(): EventEnveloper =
        OutboxEventEnveloper()

    @Bean
    fun eventOutboxStore(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        jpaEventOutboxRepository: JpaEventOutboxRepository
    ): EventOutboxStore =
        JpaEventOutboxStore(
            repository = jpaEventOutboxRepository
        )

    @Bean
    fun eventContextProvider(): EventContextProvider =
        OutboxEventContextProvider()

    @Bean
    fun outBoxEventSerializer(
        serializer: Serializer,
    ): OutboxEventSerializer =
        OutboxEventSerializerImpl(
            serializer = serializer
        )

    @Bean
    fun eventProducer(
        outboxStore: EventOutboxStore,
        eventContextProvider: EventContextProvider,
        eventSerializer: OutboxEventSerializer,
        @Value("\${spring.application.name}") applicationName: String,
    ): EventProducer =
        OutboxEventProducer(
            producer = applicationName,
            outboxStore = outboxStore,
            eventContextProvider = eventContextProvider,
            eventSerializer = eventSerializer,
        )

}