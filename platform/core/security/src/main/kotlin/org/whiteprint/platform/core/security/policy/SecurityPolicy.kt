package org.whiteprint.platform.core.security.policy

import org.whiteprint.platform.core.kernel.policy.Policy


enum class SecurityPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
) : Policy {

    TOKEN_KEY_ID_MISSING(
        401,
        "TOKEN_KEY_ID_MISSING",
        "Token key ID missing for authentication"
    ),

    TOKEN_INVALID(
        401,
        "TOKEN_INVALID",
        "The provided token is invalid."
    ),

    TOKEN_EXPIRED(
        401,
        "TOKEN_EXPIRED",
        "The provided token is expired."
    ),

    TOKEN_UNSUPPORTED(
        401,
        "TOKEN_UNSUPPORTED",
        "The provided token format is not supported."
    ),

    TOKEN_NOT_FOUND(
        401,
        "TOKEN_NOT_FOUND",
        "Authorization token is missing."
    ),

    TOKEN_NEEDS_UPDATE(
        401,
        "TOKEN_NEEDS_UPDATE",
        "User information has changed. Please refresh your token."
    ),

    TOKEN_BLACKLISTED(
        403,
        "TOKEN_BLACKLISTED",
        "Token is blacklisted by logout. Please re-login."
    ),

    TOKEN_CLAIM_INVALID(
        401,
        "TOKEN_CLAIM_INVALID",
        "The provided token contains invalid or unexpected claims."
    ),

    TOKEN_SIGNATURE_INVALID(
        401,
        "TOKEN_SIGNATURE_INVALID",
        "The token signature validation failed."
    ),

    TOKEN_VERIFICATION_INTERNAL_ERROR(
        500,
        "TOKEN_VERIFICATION_INTERNAL_ERROR",
        "An unexpected error occurred during token verification."
    ),

    TOKEN_VERIFICATION_KEY_ERROR(
        500,
        "TOKEN_VERIFICATION_KEY_ERROR",
        "Verification should be handled by Verifier with Public Key."
    ),

    PERMISSION_DENIED(
        403,
        "PERMISSION_DENIED",
        "You do not have the required permission to perform this action."
    )
}