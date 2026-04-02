package org.whiteprint.platform.adapter.event.outbox.configuration.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.context.OutboxEventContextProvider
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.JpaEventOutboxStore
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.OutboxEventEnveloper
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher.OutboxEventPublisher
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.repository.JpaEventOutboxRepository
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer.OutboxEventSerializer
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.outbox.EventContextProvider
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.outbox.EventPublisher
import javax.sql.DataSource

@Configuration
@ConditionalOnBean(DataSource::class, TopicResolver::class)
@ConditionalOnProperty(
    prefix = "adapter.event.outbox",
    name = ["infrastructureImplementation"],
    havingValue = "JPA",
    matchIfMissing = false
)
//@EnableJpaRepositories(basePackageClasses = [JpaEventOutboxRepository::class])
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
    fun eventSerializer(): EventSerializer =
        OutboxEventSerializer()

    @Bean
    fun eventPublisher(
        outboxStore: EventOutboxStore,
        eventContextProvider: EventContextProvider,
        eventSerializer: EventSerializer,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        topicResolver: TopicResolver
    ): EventPublisher =
        OutboxEventPublisher(
            outboxStore = outboxStore,
            eventContextProvider = eventContextProvider,
            eventSerializer = eventSerializer,
            topicResolver = topicResolver,
        )




}