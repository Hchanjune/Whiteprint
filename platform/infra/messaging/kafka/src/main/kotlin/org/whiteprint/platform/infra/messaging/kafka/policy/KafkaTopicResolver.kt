package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy

class KafkaTopicResolver(
    private val allowedTopics: Set<String>,
    private val prefix: String,
    private val version: String,
    private val separator: String
): TopicResolver {
    override fun resolve(envelope: EventEnvelope): String {

        if (!allowedTopics.contains(envelope.eventType)) {
            throw EventException(
                policy = EventPolicy.TOPIC_NOT_CONFIGURED,
                attributes = mapOf(
                    "topicName" to envelope.eventType
                )
            )
        }

        return listOf(
            prefix,
            envelope.eventType,
            version
        ).joinToString(separator)
    }
}