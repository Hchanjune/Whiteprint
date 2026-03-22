package org.whiteprint.platform.core.kms.model

data class KeyMaterial(
    val keyId: KeyId,
    val type: KeyType,
    val encoded: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyMaterial

        if (keyId != other.keyId) return false
        if (type != other.type) return false
        if (!encoded.contentEquals(other.encoded)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + encoded.contentHashCode()
        return result
    }
}