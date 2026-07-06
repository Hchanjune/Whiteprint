package org.whiteprint.platform.adapter.event.inbox.configuration.mongo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEnvelopeOpener
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEventConsumer
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer.ReactiveMongoEventInboxQueryStore
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer.ReactiveMongoEventInboxStore
import org.whiteprint.platform.adapter.event.inbox.configuration.serializer.InboxEventSerializerImpl
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.inbox.EventInboxQueryStore
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer

@Configuration
@ConditionalOnProperty(
    prefix = "adapter.event.inbox",
    name = ["infrastructure-implementation"],
    havingValue = "mongo",
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveMongoEventInboxConfiguration {

    @Bean
    fun inboxEventSerializer(serializer: Serializer): InboxEventSerializer =
        InboxEventSerializerImpl(serializer)

    @Bean
    fun envelopeOpener(eventSerializer: InboxEventSerializer): EnvelopeOpener =
        InboxEnvelopeOpener(eventSerializer)

    @Bean
    fun eventInboxStore(reactiveMongoTemplate: ReactiveMongoTemplate): EventInboxStore =
        ReactiveMongoEventInboxStore(reactiveMongoTemplate)

    @Bean
    fun eventInboxQueryStore(reactiveMongoTemplate: ReactiveMongoTemplate): EventInboxQueryStore =
        ReactiveMongoEventInboxQueryStore(reactiveMongoTemplate)

    @Bean
    fun eventConsumer(inboxStore: EventInboxStore, envelopeOpener: EnvelopeOpener): EventConsumer =
        InboxEventConsumer(inboxStore, envelopeOpener)
}
