package org.whiteprint.platform.core.kms.model

data class EncryptionResult(
    val keyId: KeyId,
    val cipherText: ByteArray,
    val iv: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionResult

        if (keyId != other.keyId) return false
        if (!cipherText.contentEquals(other.cipherText)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId.hashCode()
        result = 31 * result + cipherText.contentHashCode()
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        return result
    }
}
