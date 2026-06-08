package org.whiteprint.sample.user.command.application.port.`in`.event

import org.whiteprint.platform.core.messaging.model.event.external.IntegrationEvent

class AccountCreatedEvent: IntegrationEvent {
    override val eventType: String = "account.created"
    override val schemaVersion: String = "Alpha"
}