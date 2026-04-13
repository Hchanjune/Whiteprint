package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.EncryptionResult
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.SigningResult

interface KeyOperations {
    fun sign(keyAlias: String, rawText: String): SigningResult
    fun verify(keyId: KeyId, rawText: String, signature: ByteArray): Boolean

    fun signBinary(keyAlias: String, data: ByteArray): SigningResult
    fun verifyBinary(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean

    fun encrypt(alias: String, plainText: ByteArray): EncryptionResult
    fun decrypt(keyId: KeyId, cipherText: ByteArray): ByteArray
}