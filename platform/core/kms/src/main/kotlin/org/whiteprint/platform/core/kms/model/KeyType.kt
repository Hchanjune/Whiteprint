package org.whiteprint.platform.core.kms.model

enum class KeyType {
    // Asymmetric
    RSA_2048, RSA_4096, EC_P256,

    // Symmetric
    AES_128_GCM, AES_256_GCM,

    // Auth
    HMAC_SHA256
}