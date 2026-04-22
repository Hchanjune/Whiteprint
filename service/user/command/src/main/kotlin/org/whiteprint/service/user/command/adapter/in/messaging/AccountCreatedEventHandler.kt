package org.whiteprint.service.user.command.adapter.`in`.messaging

import org.springframework.stereotype.Component
import org.whiteprint.platform.adapter.event.subscriber.configuration.AbstractEventHandler
import org.whiteprint.service.user.command.application.port.`in`.event.AccountCreatedEvent
import kotlin.reflect.KClass

@Component
class AccountCreatedEventHandler: AbstractEventHandler<AccountCreatedEvent>() {

    override val eventType: String = "account.created"
    override val eventClass: KClass<AccountCreatedEvent> = AccountCreatedEvent::class

    override fun handle(event: AccountCreatedEvent) {
        println(">>> $event")
    }

}