package org.whiteprint.platform.adapter.messaging.subscriber.configuration.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.messaging.subscriber.kafka")
data class KafkaConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var topicPolicy: TopicPolicy = TopicPolicy(),
    var producer: Producer = Producer(),
    var consumer: Consumer = Consumer(),
    var retryPolicy: Retry = Retry()
) {

    data class DataSourceProperties(
        var host: String = "localhost",
        var port: Int = 9092,
    )

    data class TopicPolicy(
        var autoCreate: Boolean = true,
        var prefix: String = "wp",
        var host: String = "",
        var topic: String = "topic",
        var version: String = "v1",

        var defaultPartitions: Int = 3,
        var defaultReplicationFactor: Int = 1,
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

    data class Consumer(
        var groupId: String = "platform-group",
        var autoOffsetReset: String = "earliest",
        var concurrency: Int = 3,
        var enableAutoCommit: Boolean = false,
        var isolationLevel: String = "read_committed",
    )

    data class Retry(
        var maxAttempts: Long = 3L,
        var backoffInterval: Long = 2000L,
        var deadLetterTopicEnabled: Boolean = true,
    )

}