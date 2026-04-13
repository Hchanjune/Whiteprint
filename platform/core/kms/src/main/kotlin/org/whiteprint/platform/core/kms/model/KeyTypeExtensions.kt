package org.whiteprint.platform.core.kms.model

import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy

fun KeyType.toSignatureAlgorithm(): String = when (this) {
    KeyType.RSA_2048, KeyType.RSA_4096 -> "SHA256withRSA"   // pkcs1v15
    KeyType.EC_P256 -> "SHA256withECDSA"
    KeyType.HMAC_SHA256 -> "HmacSHA256"
    KeyType.AES_128_GCM, KeyType.AES_256_GCM -> throw KmsException(
        KmsPolicy.KMS_NOT_SUPPORTED,
        mapOf("reason" to "AES keys do not support signing: $this")
    )
}

fun KeyType.toKeyAlgorithm(): String = when (this) {
    KeyType.RSA_2048, KeyType.RSA_4096 -> "RSA"
    KeyType.EC_P256 -> "EC"
    KeyType.HMAC_SHA256 -> "HmacSHA256"
    KeyType.AES_128_GCM, KeyType.AES_256_GCM -> "AES"
}

fun KeyType.toVaultType(): String = when (this) {
    KeyType.RSA_2048 -> "rsa-2048"
    KeyType.RSA_4096 -> "rsa-4096"
    KeyType.EC_P256 -> "ecdsa-p256"
    KeyType.AES_128_GCM -> "aes128-gcm96"
    KeyType.AES_256_GCM -> "aes256-gcm96"
    KeyType.HMAC_SHA256 -> "hmac"
}

fun String.toKeyType(): KeyType = when (this.lowercase()) {
    "rsa-2048" -> KeyType.RSA_2048
    "rsa-4096" -> KeyType.RSA_4096
    "ecdsa-p256" -> KeyType.EC_P256
    "aes128-gcm96" -> KeyType.AES_128_GCM
    "aes256-gcm96" -> KeyType.AES_256_GCM
    "hmac" -> KeyType.HMAC_SHA256
    else -> throw KmsException(
        KmsPolicy.KMS_EXTERNAL_ERROR,
        mapOf("reason" to "Unsupported or unknown Vault key type: $this")
    )
}

fun KeyType.toJwtAlgorithm(): String = when (this) {
    KeyType.RSA_2048, KeyType.RSA_4096 -> "RS256"
    KeyType.EC_P256 -> "ES256"
    KeyType.HMAC_SHA256 -> "HS256"
    KeyType.AES_128_GCM, KeyType.AES_256_GCM -> throw KmsException(
        KmsPolicy.KMS_NOT_SUPPORTED,
        mapOf("reason" to "AES keys do not support JWT signing: $this")
    )
}