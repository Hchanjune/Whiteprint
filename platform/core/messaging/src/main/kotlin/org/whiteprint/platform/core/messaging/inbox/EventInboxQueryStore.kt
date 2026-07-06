package org.whiteprint.platform.core.messaging.inbox

interface EventInboxQueryStore {
    fun findByTraceId(traceId: String): List<EventProcessingStatus>
    fun findByCausationId(causationId: String): List<EventProcessingStatus>
}
