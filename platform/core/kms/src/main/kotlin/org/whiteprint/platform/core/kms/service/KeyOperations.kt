package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.EncryptionResult
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.SigningResult

interface KeyOperations {
    fun sign(alias: String, data: ByteArray): SigningResult
    fun verify(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean

    fun encrypt(alias: String, plainText: ByteArray): EncryptionResult
    fun decrypt(keyId: KeyId, cipherText: ByteArray): ByteArray
}