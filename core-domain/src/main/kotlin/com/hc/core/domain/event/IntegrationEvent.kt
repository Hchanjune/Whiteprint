package com.hc.core.domain.event

interface IntegrationEvent: Event {
    val spanId: String
    val source: String

    val sequence: Long
    val schemaVersion: String
    val retryCount: Int
    val partitionKey: String
}