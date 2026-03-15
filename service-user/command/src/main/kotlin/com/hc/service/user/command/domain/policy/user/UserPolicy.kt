package com.hc.service.user.command.domain.policy.user

import com.hc.core.kernel.policy.Policy

enum class UserPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * RequireAttributes
     * - [userId]
     * - [username]
     */
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "Such user not found: id-[[userId]] / username-[[username]]"),

    /**
     * RequireAttributes
     * - [email]
     */
    USER_EMAIL_INVALID(400, "USER_EMAIL_INVALID", "User email invalid: [[email]]"),

}