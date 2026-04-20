package org.whiteprint.platform.adapter.event.outbox.configuration.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.context.OutboxEventContextProvider
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.JpaEventOutboxStore
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.OutboxEventEnveloper
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.OutboxEventProducer
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.repository.JpaEventOutboxRepository
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer.OutboxEventSerializer
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.outbox.EventContextProvider
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.outbox.EventProducer

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
        JpaEventOutboxStore(jpaEventOutboxRepository)

    @Bean
    fun eventContextProvider(): EventContextProvider =
        OutboxEventContextProvider()

    @Bean
    fun eventSerializer(
        serializer: Serializer,
    ): EventSerializer =
        OutboxEventSerializer(
            serializer = serializer
        )

    @Bean
    fun eventProducer(
        outboxStore: EventOutboxStore,
        eventContextProvider: EventContextProvider,
        eventSerializer: EventSerializer,
        topicResolver: TopicResolver
    ): EventProducer =
        OutboxEventProducer(
            outboxStore = outboxStore,
            eventContextProvider = eventContextProvider,
            eventSerializer = eventSerializer,
            topicResolver = topicResolver,
        )

}