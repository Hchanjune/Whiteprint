package org.whiteprint.platform.core.messaging.model.event

interface PartitionedEvent {
    fun partitionKey(): Long
}