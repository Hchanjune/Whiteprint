package org.whiteprint.service.auth.domain.accounts.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class AccountPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    // Validation

    /**
     * Required Attributes
     * - [input]
     * - [reason]
     */
    INVALID_USERNAME_FORMAT(
        400,
        "INVALID_USERNAME_FORMAT",
        "Invalid username format: [[reason]] (Input: [[input]])",
    ),
    /**
     * Required Attributes
     * - [input]
     * - [reason]
     */
    INVALID_EMAIL_FORMAT(
        400,
        "INVALID_EMAIL_FORMAT",
        "Invalid email format: [[reason]] (Input: [[input]])",
    ),
    /**
     * Required Attributes
     * - [input]
     * - [reason]
     */
    INVALID_PHONE_NUMBER_FORMAT(
        400,
        "INVALID_PHONE_NUMBER_FORMAT",
        "Invalid phone number format: [[reason]] (Input: [[input]])",
    ),
    /**
     * Required Attributes
     * - [input]
     * - [reason]
     */
    INVALID_PASSWORD_FORMAT(
        400,
        "INVALID_PASSWORD_FORMAT",
        "Invalid password format: [[reason]] (Input: [[input]])",
    ),

    /**
     * Required Attributes
     * - [input]
     * - [reason]
     */
    INVALID_PASSWORD_CHECK_FORMAT(
        400,
        "INVALID_PASSWORD_FORMAT",
        "Invalid password check format: [[reason]] (Input: [[input]])",
    ),


    // Business Policy

    PASSWORD_MISS_MATCH(
        400,
        "PASSWORD_MISS_MATCH",
        "Password and password check must match.",
    )
}