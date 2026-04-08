package org.whiteprint.service.auth.domain.accounts.aggregate

import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.service.auth.domain.accounts.model.Account
import org.whiteprint.service.auth.domain.accounts.model.Credential
import org.whiteprint.service.auth.domain.accounts.vo.Email
import org.whiteprint.service.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.service.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.service.auth.domain.accounts.vo.Username
import java.time.Instant

class AccountAggregate private constructor(
    private val account: Account,
    private val credential: Credential
): Aggregate<Account>() {

    override val schemaVersion: String
        get() = "ALPHA"
    override val id: Long
        get() = account.id
    override val root: Account
        get() = account
    override val insertedAt: Instant
        get() = account.insertedAt
    override val updatedAt: Instant
        get() = account.updatedAt
    override val version: Long
        get() = account.version
    override val isDeleted: Boolean
        get() = account.isDeleted
    override val deletedAt: Instant?
        get() = account.deletedAt


    companion object {
        fun signup(
            username: Username,
            email: Email,
            phoneNumber: PhoneNumber,
            passwordHash: PasswordHash
        ): AccountAggregate {
            val account = Account(
                id = TsidGenerator.generate(),
                username = username,
                email = email,
                phoneNumber = phoneNumber,
                insertedAt = Instant.now(),
                updatedAt = Instant.now(),
                version = 0L,
                isDeleted = false,
                deletedAt = null
            )
            val credential = Credential(
                id = TsidGenerator.generate(),
                accountId = account.id,
                passwordHash = passwordHash,
            )
            return AccountAggregate(
                account = account,
                credential = credential
            )
        }
    }

}