package org.whiteprint.platform.adapter.event.subscriber.configuration.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.subscriber.kafka")
data class KafkaConsumerConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var consumer: Consumer = Consumer(),
    var subscriptionPolicy: SubscriptionPolicy = SubscriptionPolicy(),
    var errorHandling: ErrorHandling = ErrorHandling(),
) {

    data class DataSourceProperties(
        var host: String = "localhost",
        var port: Int = 9092,
    )

    data class Consumer(
        var groupId: String = "platform-group",
        var autoOffsetReset: String = "earliest",
        var concurrency: Int = 3,
        var enableAutoCommit: Boolean = false,
        var isolationLevel: String = "read_committed",
    )

    data class SubscriptionPolicy(
        var prefix: String = "wp",
        var version: String = "v1",
        var separator: String = ".",
        var eventTypes: Set<String> = emptySet(),
    )

    data class ErrorHandling(
        var retry: Retry = Retry(),
        var deadLetter: DeadLetter = DeadLetter()
    ) {

        data class Retry(
            var maxAttempts: Long = 3L,
            var backoffInterval: Long = 2000L,
        )

        data class DeadLetter(
            var enabled: Boolean = true,
            var topicSuffix: String = ".DLT",
            var producer: Producer = Producer()
        ) {
            data class Producer(
                var acks: String = "all",
                var batchSize: Int = 16384,
                var lingerMillis: Int = 5,
                var compressionType: String = "lz4",
                var retries: Int = Int.MAX_VALUE,
            )
        }

    }

}