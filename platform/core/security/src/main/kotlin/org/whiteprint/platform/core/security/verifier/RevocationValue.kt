package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.policy.RevocationReason

sealed class RevocationValue {
    data class Token(val reason: RevocationReason) : RevocationValue()
    data class Account(val reason: RevocationReason, val revokedAt: Long) : RevocationValue()

    fun serialize(): String = when (this) {
        is Token -> reason.name
        is Account -> "${reason.name}:${revokedAt}"
    }

    companion object {
        fun deserializeToken(value: String): Token =
            Token(RevocationReason.valueOf(value))

        fun deserializeAccount(value: String): Account {
            val (reason, timestamp) = value.split(":")
            return Account(RevocationReason.valueOf(reason), timestamp.toLong())
        }
    }
}