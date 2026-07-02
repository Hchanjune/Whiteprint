package org.whiteprint.sample.user.command.adapter.`in`.messaging

import io.github.hchanjune.omk.core.annotations.ManagedEventHandler
import org.springframework.stereotype.Component
import org.whiteprint.platform.adapter.event.subscriber.configuration.AbstractEventHandler
import org.whiteprint.sample.user.command.application.port.`in`.event.AccountCreatedEvent
import kotlin.reflect.KClass

@Component
class AccountCreatedEventHandler: AbstractEventHandler<AccountCreatedEvent>() {

    override val eventType: String = "account.created"
    override val eventClass: KClass<AccountCreatedEvent> = AccountCreatedEvent::class

    @ManagedEventHandler
    override suspend fun handle(event: AccountCreatedEvent) {
        println(">>> $event")
    }

}