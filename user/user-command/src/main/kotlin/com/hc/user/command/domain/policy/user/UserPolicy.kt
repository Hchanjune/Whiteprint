package com.hc.user.command.domain.policy.user

import com.hc.core.domain.exception.DomainPolicyException

sealed class UserPolicy(code: String, message: String): DomainPolicyException(code, message) {

    class UserNotFoundException(identifier: String) : UserPolicy(
        code = "USER",
        message = "$identifier User does not exist"
    )

    class UserEmailInvalidException(email: String): UserPolicy(
        code = "USER_400_001",
        message = "Invalid email: $email"
    )

}