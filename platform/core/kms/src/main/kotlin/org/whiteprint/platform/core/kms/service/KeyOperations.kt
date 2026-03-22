package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyId

interface KeyOperations {
    fun encrypt(keyId: KeyId, plainText: ByteArray, context: Map<String, String>? = null): ByteArray
    fun decrypt(keyId: KeyId, cipherText: ByteArray, context: Map<String, String>? = null): ByteArray

    fun sign(keyId: KeyId, data: ByteArray): ByteArray
    fun verify(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean
}