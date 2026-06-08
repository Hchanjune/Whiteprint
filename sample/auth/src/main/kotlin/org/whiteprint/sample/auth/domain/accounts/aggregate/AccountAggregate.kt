package org.whiteprint.sample.auth.domain.accounts.aggregate

import org.springframework.security.crypto.password.PasswordEncoder
import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.outbox.EventRecorder
import org.whiteprint.sample.auth.domain.accounts.model.Account
import org.whiteprint.sample.auth.domain.accounts.model.Credential
import org.whiteprint.sample.auth.domain.accounts.vo.AccountLock
import org.whiteprint.sample.auth.domain.accounts.vo.Email
import org.whiteprint.sample.auth.domain.accounts.vo.MultiFactorAuth
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHistory
import org.whiteprint.sample.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.sample.auth.domain.accounts.vo.RawPassword
import org.whiteprint.sample.auth.domain.accounts.vo.Username
import java.time.Instant

class AccountAggregate (
    private val account: Account,
    private val credential: Credential,
): Aggregate<Account>(), EventRecorder {

    private val _events: MutableList<Event> = mutableListOf()
    override fun record(event: Event) { _events.add(event) }
    override fun pullEvents(): List<Event> {
        val pulled = _events.toList()
        _events.clear()
        return pulled
    }

    override val schemaVersion get() = "ALPHA"
    override val id get() = account.id
    override val root get() = account

    override val insertedAt get() = account.insertedAt
    override val updatedAt get() = account.updatedAt
    override val version get() = account.version
    override val isDeleted get() = account.isDeleted
    override val deletedAt get() = account.deletedAt

    val username get() = account.username
    val email get() = account.email
    val phoneNumber get() = account.phoneNumber

    val credentialId get() = credential.id
    val passwordHash get() = credential.passwordHash
    val passwordUpdatedAt get() = credential.passwordUpdatedAt
    val passwordExpiredAt get() = credential.passwordExpiredAt
    val failedAttempts get() = credential.failedAttempts
    val isLocked get() = credential.isLocked
    val lockedReason get() = credential.lockedReason
    val lockedAt get() = credential.lockedAt
    val lastLoginAt get() = credential.lastLoginAt
    val lastFailedAt get() = credential.lastFailedAt
    val mfa get() = credential.mfa
    val passwordHistory get() = credential.passwordHistory

    fun login(rawPassword: RawPassword, passwordEncoder: PasswordEncoder): Boolean {
        return if (passwordEncoder.matches(rawPassword.value, passwordHash.value)) {
            credential.recordLoginSuccess()
            true
        } else {
            credential.recordLoginFailure()
            false
        }
    }

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
                passwordUpdatedAt = Instant.now(),
                passwordExpiredAt = null,
                failedAttempts = 0,
                isLocked = false,
                lockedReason = AccountLock.NONE,
                lockedAt = null,
                lastLoginAt = null,
                lastFailedAt = null,
                mfa = MultiFactorAuth(null),
                passwordHistory = PasswordHistory()
            )
            return AccountAggregate(
                account = account,
                credential = credential
            )
        }
    }

}