package org.whiteprint.sample.auth.application.port.out.persistence

import org.whiteprint.platform.core.domain.repository.AggregateRepository
import org.whiteprint.sample.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.sample.auth.domain.accounts.vo.AccountIdentifier
import org.whiteprint.sample.auth.domain.accounts.vo.Email
import org.whiteprint.sample.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.sample.auth.domain.accounts.vo.Username

interface AccountRepository: AggregateRepository<Long, AccountAggregate> {

    fun existsByUsername(username: Username): Boolean

    fun existsByEmail(email: Email): Boolean

    fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean

    fun findByUsernameOrThrow(username: Username): AccountAggregate

    fun findByEmailOrThrow(email: Email): AccountAggregate

    fun findByPhoneNumberOrThrow(phoneNumber: PhoneNumber): AccountAggregate

    fun findByIdentifierOrThrow(identifier: AccountIdentifier): AccountAggregate

}