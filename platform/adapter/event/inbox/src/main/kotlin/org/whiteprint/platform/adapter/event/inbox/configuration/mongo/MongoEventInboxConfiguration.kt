package org.whiteprint.platform.adapter.event.inbox.configuration.mongo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEnvelopeOpener
import org.whiteprint.platform.adapter.event.inbox.configuration.serializer.InboxEventSerializerImpl
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer.MongoEventInboxStore
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.repository.MongoEventInboxRepository
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEventConsumer

@Configuration
@ConditionalOnProperty(
    prefix = "adapter.event.inbox",
    name = ["infrastructure-implementation"],
    havingValue = "mongo",
)
@EnableMongoRepositories(basePackageClasses = [MongoEventInboxRepository::class])
class MongoEventInboxConfiguration {

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun inboxEventSerializer(serializer: Serializer): InboxEventSerializer =
        InboxEventSerializerImpl(serializer)

    @Bean
    fun envelopeOpener(eventSerializer: InboxEventSerializer): EnvelopeOpener =
        InboxEnvelopeOpener(eventSerializer)

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun eventInboxStore(
        repository: MongoEventInboxRepository,
        mongoTemplate: MongoTemplate,
    ): EventInboxStore = MongoEventInboxStore(repository, mongoTemplate)

    @Bean
    fun eventConsumer(inboxStore: EventInboxStore, envelopeOpener: EnvelopeOpener): EventConsumer =
        InboxEventConsumer(inboxStore, envelopeOpener)

}
