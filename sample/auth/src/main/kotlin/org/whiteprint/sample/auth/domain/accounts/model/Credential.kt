package org.whiteprint.sample.auth.domain.accounts.model

import org.whiteprint.platform.core.domain.model.contract.Identifiable
import org.whiteprint.sample.auth.domain.accounts.vo.AccountLock
import org.whiteprint.sample.auth.domain.accounts.vo.MultiFactorAuth
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHistory
import java.time.Instant

data class Credential(
    override val id: Long,
    val accountId: Long,
    val passwordHash: PasswordHash,
    val passwordUpdatedAt: Instant,
    val passwordExpiredAt: Instant?,
    var failedAttempts: Int,
    val isLocked: Boolean,
    val lockedReason: AccountLock,
    val lockedAt: Instant?,
    var lastLoginAt: Instant?,
    var lastFailedAt: Instant?,
    val mfa: MultiFactorAuth,
    val passwordHistory: PasswordHistory
): Identifiable<Long> {

    fun recordLoginFailure() {
        failedAttempts++
        lastFailedAt = Instant.now()
    }

    fun recordLoginSuccess() {
        failedAttempts = 0
        lastLoginAt = Instant.now()
    }

}