package org.whiteprint.service.auth.domain.accounts.vo

import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountValidationException

@JvmInline
value class Username(val value: String) {

    companion object {
        /**
         * 1. 4-50 Character
         * 2. starting with a lowercase letter
         * 3. numeric, lowercase available only
         */
        private val REGEX = Regex("^[a-z][a-z0-9]{3,49}$")

        private const val VALIDATION_REASON =
            "Username must be 4-50 characters long, starting with a lowercase letter, and consist only of lowercase letters and numbers without special characters."
    }

    init {
        if (!value.matches(REGEX)) {
            throw AccountValidationException(
                AccountPolicy.INVALID_USERNAME_FORMAT,
                mapOf(
                    "input" to value,
                    "reason" to VALIDATION_REASON
                )
            )
        }
    }

}