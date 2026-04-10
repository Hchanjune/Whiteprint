package org.whiteprint.service.auth.application.port.out

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.whiteprint.platform.core.domain.repository.AggregateRepository
import org.whiteprint.service.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.service.auth.domain.accounts.vo.AccountIdentifier
import org.whiteprint.service.auth.domain.accounts.vo.Email
import org.whiteprint.service.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.service.auth.domain.accounts.vo.Username

@ManagedRepository
interface AccountRepository: AggregateRepository<Long, AccountAggregate> {

    fun existsByUsername(username: Username): Boolean

    fun existsByEmail(email: Email): Boolean

    fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean

    fun findByUsernameOrThrow(username: Username): AccountAggregate

    fun findByEmailOrThrow(email: Email): AccountAggregate

    fun findByPhoneNumberOrThrow(phoneNumber: PhoneNumber): AccountAggregate

    fun findByIdentifierOrThrow(identifier: AccountIdentifier): AccountAggregate

}