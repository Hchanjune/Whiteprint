package org.whiteprint.platform.core.kms.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class KmsPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * Required Attributes
     * - [input]
     */
    INVALID_KEY_ID_FORMAT(500, "INVALID_KEY_ID_FORMAT", "Invalid KeyId format. Expected 'alias:version' but got '[[input]]'"),

    /**
     * Required Attributes
     * - [alias]
     * - [version]
     */
    MATERIAL_NOT_FOUND(404, "MATERIAL_NOT_FOUND", "MATERIAL_NOT_FOUND alias: [[alias]], version: [[version]]"),

    /**
     * Required Attributes
     * - [keyId]
     */
    KEY_NOT_FOUND(404, "KEY_NOT_FOUND", "KeyId [[keyId]] not found from KMS"),

    /**
     * Required Attributes
     * - [keyId]
     * - [operation]
     */
    INVALID_KEY_ALGORITHM(400, "INVALID_KEY_ALGORITHM", "Key [[keyId]] does not support [[operation]]"),

    /**
     * Required Attributes
     * - [keyId]
     */
    KEY_EXPIRED(403, "KEY_EXPIRED", "Key [[keyId]] has expired at [[expiresAt]]"),

    /**
     * Required Attributes
     * - [reason]
     */
    KMS_EXTERNAL_ERROR(502, "KMS_EXTERNAL_ERROR", "External KMS provider error: [[reason]]")

}