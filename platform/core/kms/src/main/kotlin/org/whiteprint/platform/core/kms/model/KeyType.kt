package org.whiteprint.platform.core.kms.model

enum class KeyType(
    val category: KeyCategory,
    val algorithm: String,
    val isExportable: Boolean
) {
    // --- Asymmetric (Signing & Encryption) ---
    RSA_2048(KeyCategory.ASYMMETRIC, "RSA", false),
    RSA_4096(KeyCategory.ASYMMETRIC, "RSA", false),
    EC_P256(KeyCategory.ASYMMETRIC, "EC", false),

    // --- Symmetric (Encryption) ---
    AES_128_GCM(KeyCategory.SYMMETRIC, "AES", true),
    AES_256_GCM(KeyCategory.SYMMETRIC, "AES", true),

    // --- Auth (MAC) ---
    HMAC_SHA256(KeyCategory.AUTH, "HMAC", true);

    val isAsymmetric: Boolean get() = category == KeyCategory.ASYMMETRIC
    val isSymmetric: Boolean get() = category == KeyCategory.SYMMETRIC
}