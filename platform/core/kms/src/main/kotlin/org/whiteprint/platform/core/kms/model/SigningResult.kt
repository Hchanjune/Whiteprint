package org.whiteprint.platform.core.kms.model

data class SigningResult(
    val keyId: KeyId,
    val signature: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SigningResult

        if (keyId != other.keyId) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}
