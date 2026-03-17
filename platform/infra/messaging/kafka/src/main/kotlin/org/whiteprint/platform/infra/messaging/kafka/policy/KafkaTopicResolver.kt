package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.messaging.policy.TopicResolver

class KafkaTopicResolver(
    private val prefix: String,
    private val host: String,
    private val topic: String,
    private val version: String
): TopicResolver {
    override fun resolve(): String {
        return listOf(prefix, host, topic, version)
            .filter { it.isNotBlank() }
            .joinToString(".")
    }
}