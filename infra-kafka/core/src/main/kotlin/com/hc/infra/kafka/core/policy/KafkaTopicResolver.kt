package com.hc.infra.kafka.core.policy

import com.hc.core.messaging.policy.TopicResolver

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