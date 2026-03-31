package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.messaging.contract.TopicResolver

class KafkaTopicResolver(
    private val prefix: String,
    private val topic: String,
    private val version: String,
    private val separator: String
): TopicResolver {
    override fun resolve(): String {
        return listOf(prefix, topic, version)
            .filter { it.isNotBlank() }
            .joinToString(separator)
    }
}