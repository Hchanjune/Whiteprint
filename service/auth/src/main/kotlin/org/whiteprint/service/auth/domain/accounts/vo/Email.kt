package org.whiteprint.service.auth.domain.accounts.vo

import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountValidationException

@JvmInline
value class Email(override val value: String): AccountIdentifier {

    companion object {
        /**
         * RFC 5322
         */
        private val REGEX = Regex("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")

        private const val VALIDATION_REASON =
            "Email must follow the standard format (e.g., user@example.com) and not exceed 320 characters."
    }

    init {
        if (!value.matches(REGEX)) {
            throw AccountValidationException(
                AccountPolicy.INVALID_EMAIL_FORMAT,
                mapOf(
                    "input" to value,
                    "reason" to VALIDATION_REASON
                )
            )
        }

        if (value.length > 320) {
            throw AccountValidationException(
                AccountPolicy.INVALID_EMAIL_FORMAT,
                mapOf(
                    "input" to value.take(20) + "...",
                    "reason" to "Email address is too long. Max length is 320 characters."
                )
            )
        }
    }
}