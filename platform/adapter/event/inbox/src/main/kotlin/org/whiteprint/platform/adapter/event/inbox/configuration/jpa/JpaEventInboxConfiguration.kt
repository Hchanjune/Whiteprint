package org.whiteprint.platform.adapter.event.inbox.configuration.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEnvelopeOpener
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.InboxEventConsumer
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer.JpaEventInboxStore
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository.JpaEventInboxRepository
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.serializer.InboxEventSerializerImpl
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EnvelopeOpener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer

@Configuration
@ConditionalOnProperty(
    prefix = "adapter.event.inbox",
    name = ["infrastructure-implementation"],
    havingValue = "jpa",
    matchIfMissing = false
)
@EnableJpaRepositories(basePackageClasses = [JpaEventInboxRepository::class])
class JpaEventInboxConfiguration {

    @Bean
    fun inboxEventSerializer(
        serializer: Serializer,
    ): InboxEventSerializer =
        InboxEventSerializerImpl(
            serializer = serializer
        )

    @Bean
    fun envelopeOpener(
        eventSerializer: InboxEventSerializer,
    ): EnvelopeOpener =
        InboxEnvelopeOpener(
            eventSerializer = eventSerializer
        )

    @Bean
    fun eventInboxStore(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        jpaEventInboxRepository: JpaEventInboxRepository,
    ): EventInboxStore =
        JpaEventInboxStore(
            repository = jpaEventInboxRepository
        )

    @Bean
    fun eventConsumer(
        inboxStore: EventInboxStore,
        envelopeOpener: EnvelopeOpener
    ): EventConsumer =
        InboxEventConsumer(
            inboxStore = inboxStore,
            envelopeOpener = envelopeOpener
        )

}