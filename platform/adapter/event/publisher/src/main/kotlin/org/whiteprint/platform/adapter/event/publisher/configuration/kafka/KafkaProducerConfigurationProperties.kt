package org.whiteprint.platform.adapter.event.publisher.configuration.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.publisher.kafka")
data class KafkaProducerConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var topicPolicy: TopicPolicy = TopicPolicy(),
    var producer: Producer = Producer(),
    var publish: Publish = Publish(),
    var connectionValidation: ConnectionValidation = ConnectionValidation(),
) {

    data class ConnectionValidation(
        var enabled: Boolean = false,
    )

    data class DataSourceProperties(
        var host: String = "localhost",
        var port: Int = 9092,
    )

    data class TopicPolicy(
        var autoCreate: Boolean = true,
        var prefix: String = "wp",
        var version: String = "v1",
        var separator: String = ".",
        var topics: Map<String, TopicSpec> = emptyMap()
    )

    data class TopicSpec(
        var partitions: Int = 3,
        var replicationFactor: Int = 1,
        var retentionMillis: Long = 604800000,
        var cleanupPolicy: String = "delete",
    )

    data class Producer(
        var acks: String = "all",
        var batchSize: Int = 16384,
        var lingerMillis: Int = 5,
        var compressionType: String = "lz4",
        var retries: Int = Int.MAX_VALUE,
    )

    data class Publish(
        var retry: Retry = Retry(),
    ) {
        data class Retry(
            var maxAttempts: Long = 3L,
            var backoffInterval: Long = 2000L,
        )
    }



}