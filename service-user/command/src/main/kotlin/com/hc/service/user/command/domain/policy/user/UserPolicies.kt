package com.hc.service.user.command.domain.policy.user

import com.hc.core.exception.ErrorCode

enum class UserPolicies(
    override val status: Int,
    override val code: String,
    override val message: String,
): ErrorCode {

    USER_NOT_FOUND(404, "USER_NOT_FOUND", "User does not exist"),

    USER_EMAIL_INVALID(400, "USER_EMAIL_INVALID", "User email invalid"),

}