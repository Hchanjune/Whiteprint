package org.whiteprint.platform.adapter.event.outbox.configuration.mongo

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.context.OutboxEventContextProvider
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.producer.OutboxEventEnveloper
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.producer.OutboxEventProducer
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer.OutboxEventSerializerImpl
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.producer.MongoEventOutboxStore
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.repository.MongoEventOutboxRepository
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
    havingValue = "mongo",
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMongoRepositories(basePackageClasses = [MongoEventOutboxRepository::class])
class MongoEventOutboxConfiguration {

    @Bean
    fun eventEnveloper(): EventEnveloper = OutboxEventEnveloper()

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun eventOutboxStore(
        repository: MongoEventOutboxRepository,
        mongoTemplate: MongoTemplate,
    ): EventOutboxStore = MongoEventOutboxStore(repository, mongoTemplate)

    @Bean
    fun eventContextProvider(): EventContextProvider = OutboxEventContextProvider()

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun outBoxEventSerializer(serializer: Serializer): OutboxEventSerializer =
        OutboxEventSerializerImpl(serializer)

    @Bean
    fun eventProducer(
        outboxStore: EventOutboxStore,
        eventContextProvider: EventContextProvider,
        eventSerializer: OutboxEventSerializer,
        @Value("\${spring.application.name}") applicationName: String,
    ): EventProducer = OutboxEventProducer(
        producer = applicationName,
        outboxStore = outboxStore,
        eventContextProvider = eventContextProvider,
        eventSerializer = eventSerializer,
    )

}
