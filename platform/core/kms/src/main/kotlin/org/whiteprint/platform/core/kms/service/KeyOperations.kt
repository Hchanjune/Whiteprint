package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.EncryptionResult
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.SigningResult

interface KeyOperations {
    fun sign(keyAlias: String, rawText: String): SigningResult

    /**
     * [keyId] 의 버전으로 서명한다. 서명 전에 버전을 먼저 알아 둔 경우(JWT 헤더의 `ver`)에 쓴다 —
     * 버전 없이 서명하면 그 사이 키가 회전했을 때 헤더와 서명의 버전이 어긋난다.
     */
    fun sign(keyId: KeyId, rawText: String): SigningResult
    fun verify(keyId: KeyId, rawText: String, signature: ByteArray): Boolean

    fun signBinary(keyAlias: String, data: ByteArray): SigningResult
    fun verifyBinary(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean

    fun encrypt(alias: String, plainText: ByteArray): EncryptionResult
    fun decrypt(keyId: KeyId, cipherText: ByteArray): ByteArray
}