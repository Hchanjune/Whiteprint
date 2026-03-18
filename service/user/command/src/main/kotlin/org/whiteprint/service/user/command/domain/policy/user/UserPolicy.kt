package org.whiteprint.service.user.command.domain.policy.user

import org.whiteprint.platform.core.kernel.policy.Policy

enum class UserPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * RequireAttributes One of Followings
     * - [userId]
     * - [email]
     * - [username]
     */
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "Such user not found: id:[[userId]] email:[[email]] username:[[username]]"),

    /**
     * RequireAttributes
     * - [email]
     */
    USER_EMAIL_INVALID(400, "USER_EMAIL_INVALID", "User email invalid: [[email]]"),

}