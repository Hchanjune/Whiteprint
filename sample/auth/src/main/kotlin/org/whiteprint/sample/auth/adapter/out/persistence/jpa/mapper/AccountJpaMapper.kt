package org.whiteprint.sample.auth.adapter.out.persistence.jpa.mapper

import org.springframework.stereotype.Component
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.infra.persistence.jpa.entity.ensureExistsOrThrow
import org.whiteprint.platform.infra.persistence.jpa.entity.withId
import org.whiteprint.sample.auth.adapter.out.persistence.jpa.entity.AccountEntity
import org.whiteprint.sample.auth.adapter.out.persistence.jpa.entity.CredentialEntity
import org.whiteprint.sample.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.sample.auth.domain.accounts.model.Account
import org.whiteprint.sample.auth.domain.accounts.model.Credential
import org.whiteprint.sample.auth.domain.accounts.vo.AccountLock
import org.whiteprint.sample.auth.domain.accounts.vo.Email
import org.whiteprint.sample.auth.domain.accounts.vo.MultiFactorAuth
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHistory
import org.whiteprint.sample.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.sample.auth.domain.accounts.vo.Username

@Component
class AccountJpaMapper(
    private val serializer: Serializer
) {

    fun createEntity(domain: AccountAggregate): AccountEntity {
        val accountEntity = AccountEntity(
            username = domain.username.value,
            email = domain.email.value,
            phoneNumber = domain.phoneNumber.value,
        ).withId(domain.id)
        val credentialEntity = CredentialEntity(
            passwordHash = domain.passwordHash.value,
            passwordUpdatedAt = domain.passwordUpdatedAt,
            passwordExpiredAt = domain.passwordExpiredAt,
            failedAttempts = domain.failedAttempts,
            isLocked = domain.isLocked,
            lockedReason = domain.lockedReason.name,
            lockedAt = domain.lockedAt,
            lastLoginAt = domain.lastLoginAt,
            lastFailedAt = domain.lastFailedAt,
            mfaSecret = domain.mfa.secret,
            passwordHistory = serializer.serializeToJson(domain.passwordHistory.hashes)
        ).withId(domain.credentialId)
        accountEntity.applyCredential(credentialEntity)
        return accountEntity
    }

    fun updateEntity(domain: AccountAggregate, entity: AccountEntity) {
        entity.username = domain.username.value
        entity.email = domain.email.value
        entity.phoneNumber = domain.phoneNumber.value
        val credentialEntity = entity.credential.ensureExistsOrThrow(entity.id)
        credentialEntity.passwordHash = domain.passwordHash.value
        credentialEntity.passwordUpdatedAt = domain.passwordUpdatedAt
        credentialEntity.passwordExpiredAt = domain.passwordExpiredAt
        credentialEntity.failedAttempts = domain.failedAttempts
        credentialEntity.isLocked = domain.isLocked
        credentialEntity.lockedReason = domain.lockedReason.name
        credentialEntity.lockedAt = domain.lockedAt
        credentialEntity.lastLoginAt = domain.lastLoginAt
        credentialEntity.lastFailedAt = domain.lastFailedAt
        credentialEntity.mfaSecret = domain.mfa.secret
        credentialEntity.passwordHistory = serializer.serializeToJson(domain.passwordHistory.hashes)
    }

    fun toAggregate(entity: AccountEntity): AccountAggregate {
        val credEntity = entity.credential.ensureExistsOrThrow(entity.id)

        val account = Account(
            id = entity.id,
            username = Username(entity.username),
            email = Email(entity.email),
            phoneNumber = PhoneNumber(entity.phoneNumber),
            insertedAt = entity.insertedAt,
            updatedAt = entity.updatedAt,
            version = entity.version,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt
        )

        val historyStrings = if (!credEntity.passwordHistory.isNullOrBlank()) {
            serializer.deserializeFromJson(credEntity.passwordHistory!!, Array<String>::class.java).toList()
        } else {
            emptyList()
        }
        val credential = Credential(
            id = credEntity.id,
            accountId = entity.id,
            passwordHash = PasswordHash(credEntity.passwordHash),
            passwordUpdatedAt = credEntity.passwordUpdatedAt,
            passwordExpiredAt = credEntity.passwordExpiredAt,
            failedAttempts = credEntity.failedAttempts,
            isLocked = credEntity.isLocked,
            lockedReason = AccountLock.valueOf(credEntity.lockedReason?: "NONE"),
            lockedAt = credEntity.lockedAt,
            lastLoginAt = credEntity.lastLoginAt,
            lastFailedAt = credEntity.lastFailedAt,
            mfa = MultiFactorAuth(credEntity.mfaSecret),
            passwordHistory = PasswordHistory(
                hashes = historyStrings.map { PasswordHash(it) }
            )
        )

        return AccountAggregate(account, credential)
    }

}