package org.whiteprint.sample.auth.domain.accounts.vo

import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.sample.auth.domain.accounts.policy.AccountValidationException

@JvmInline
value class RawPasswordCheck(val value: String) {

    companion object {
        /**
         * 1. (?=.*\\d) : at least one numeric character
         * 2. (?=.*[@$!%*?&]) : at least one special character
         * 3. [A-Za-z\\d@$!%*?&] : all characters available
         * 4. {8,128} : 8 - 128
         */
        private val REGEX = Regex("^(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$")

        private const val VALIDATION_REASON =
            "Password Check must be 8-128 characters long and include at least one number and one special character."
    }

    init {
        if (!value.matches(REGEX)) {
            throw AccountValidationException(
                AccountPolicy.INVALID_PASSWORD_CHECK_FORMAT,
                mapOf(
                    "input" to "********",
                    "reason" to VALIDATION_REASON
                )
            )
        }
    }
}