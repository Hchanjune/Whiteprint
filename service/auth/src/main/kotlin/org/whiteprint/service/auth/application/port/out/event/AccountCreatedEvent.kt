package org.whiteprint.service.auth.application.port.out.event

import org.whiteprint.platform.core.messaging.model.event.PartitionedEvent
import org.whiteprint.platform.core.messaging.model.event.external.IntegrationEvent

data class AccountCreatedEvent(
    val accountId: Long
): IntegrationEvent, PartitionedEvent {
    override val eventType: String = "account.created"
    override val schemaVersion: String = "Alpha"
    override fun partitionKey(): Long = accountId
}