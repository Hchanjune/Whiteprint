package org.whiteprint.platform.core.messaging.model.event

/**
 * Marker for events that require partition-aware ordering.
 * Events sharing the same partition key are guaranteed to be
 * delivered to the same partition, preserving processing order.
 */
interface PartitionedEvent {
    fun partitionKey(): Long
}