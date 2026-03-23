package org.whiteprint.platform.core.kms.model

import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy

fun KeyType.toJavaAlgorithm(): String = when (this) {
    // Asymmetric
    KeyType.RSA_2048, KeyType.RSA_4096 -> "RSA"
    KeyType.EC_P256 -> "EC"

    // Symmetric
    KeyType.AES_128_GCM, KeyType.AES_256_GCM -> "AES"

    // Auth (Mac/SecretKeySpec용)
    KeyType.HMAC_SHA256 -> "HmacSHA256"
}

fun KeyType.toVaultType(): String = when (this) {
    KeyType.RSA_2048 -> "rsa-2048"
    KeyType.RSA_4096 -> "rsa-4096"
    KeyType.EC_P256 -> "ecdsa-p256"
    KeyType.AES_128_GCM -> "aes128-gcm96"
    KeyType.AES_256_GCM -> "aes256-gcm96"
    KeyType.HMAC_SHA256 -> "hmac"
}

fun String.toKeyType(): KeyType = when (this) {
    "rsa-2048" -> KeyType.RSA_2048
    "rsa-4096" -> KeyType.RSA_4096
    "ecdsa-p256" -> KeyType.EC_P256
    "aes128-gcm96" -> KeyType.AES_128_GCM
    "aes256-gcm96" -> KeyType.AES_256_GCM
    "hmac" -> KeyType.HMAC_SHA256
    else -> throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Unknown vault key type: $this"))
}