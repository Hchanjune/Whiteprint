package org.whiteprint.service.auth.domain.accounts.vo

import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountValidationException

@JvmInline
value class PhoneNumber(val value: String) {

    companion object {
        /**
         * E.164
         * 1. Must starts with '+'
         * 2. country code can not start with 0
         * 3. rest of numeric characters must be length in 1-14 (total 15)
         */
        private val REGEX = Regex("^\\+[1-9]\\d{1,14}$")

        private const val VALIDATION_REASON =
            "Phone number must follow the E.164 international format (e.g., +821012345678). " +
            "It must start with '+' followed by the country code and number, without spaces or hyphens."
    }

    init {
        if (!value.matches(REGEX)) {
            throw AccountValidationException(
                AccountPolicy.INVALID_PHONE_NUMBER_FORMAT,
                mapOf(
                    "input" to value,
                    "reason" to VALIDATION_REASON
                )
            )
        }
    }

}