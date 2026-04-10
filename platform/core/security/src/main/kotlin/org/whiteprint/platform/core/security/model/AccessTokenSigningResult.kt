package org.whiteprint.platform.core.security.model

data class AccessTokenSigningResult(
    val keyAlias: String,
    val keyVersion: String?,
    val algorithm: String,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessTokenSigningResult

        if (keyAlias != other.keyAlias) return false
        if (keyVersion != other.keyVersion) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyAlias.hashCode()
        result = 31 * result + keyVersion.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}