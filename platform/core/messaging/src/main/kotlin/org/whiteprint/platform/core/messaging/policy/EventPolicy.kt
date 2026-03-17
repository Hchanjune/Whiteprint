package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class EventPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    INVALID_TOPIC_FORMAT(500, "EVENT_INVALID_TOPIC_FORMAT", "Invalid topic format."),

    TOPIC_NOT_CONFIGURED(500, "EVENT_TOPIC_NOT_CONFIGURED", "EVENT_TOPIC_NOT_CONFIGURED, Please config topic in application.yml "),

    /**
     * Require Stacktrace
     *
     * RequiredAttributes
     * - [topic]
     * - [partitionKey]
     * - [eventId]
     * - [eventName]
     */
    SERIALIZATION_FAILED(500, "EVENT_SERIALIZATION_FAILED", "Event Serialization failed topic:[[topic]] partitionKey:[[partitionKey]] eventId:[[eventId]] eventName:[[eventName]]"),

    /**
     * RequiredAttributes
     * - [topic]
     * - [dataSize]
     */
    DESERIALIZATION_FAILED(500, "EVENT_DESERIALIZATION_FAILED", "Event Deserialization failed topic:[[topic]] dataSize:[[dataSize]]"),

}