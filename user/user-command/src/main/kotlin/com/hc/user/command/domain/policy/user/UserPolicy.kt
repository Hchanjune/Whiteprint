package com.hc.user.command.domain.policy.user

import com.hc.core.domain.exception.DomainPolicyException

sealed class UserPolicy(code: String, message: String): DomainPolicyException(code, message) {

    class UserEmailInvalidException(email: String): UserPolicy(
        code = "USER_400_001",
        message = "Invalid email: $email"
    )

}