package com.hc.infra.messaging.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kafka")
data class KafkaConfigurationProperties(
    var host: String = "localhost",
    var port: Int = 9092,
    var producer: Producer = Producer(),
    var listener: Listener = Listener(),
    var retry: Retry = Retry()
) {

    data class Producer(
        var prefix: String = "",
        var host: String = "",
        var topic: String = "topic",
        var version: String = "v1",
        var partitions: Int = 3,
        var replicationFactor: Int = 1,
    )

    data class Listener(
        var concurrency: Int = 3
    )

    data class Retry(
        var maxAttempts: Long = 3L,
        var backoffInterval: Long = 2000L
    )

}