package org.whiteprint.platform.core.kms.model

import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import java.security.Key
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

data class KeyMaterial(
    val keyId: KeyId,
    val type: KeyType,
    val side: KeySide,
    val encoded: ByteArray
) {

    fun toJavaKey(): Key {
        return when (side) {
            KeySide.PUBLIC -> toPublicKey()
            KeySide.PRIVATE -> throw KmsException(policy = KmsPolicy.ASYMMETRIC_PRIVATE_KEY_FORBIDDEN)
            KeySide.SECRET -> toSecretKey()
        }
    }

    fun toPublicKey(): PublicKey {
        val spec = X509EncodedKeySpec(encoded)
        return KeyFactory.getInstance(type.toSignatureAlgorithm()).generatePublic(spec)
    }

    fun toSecretKey(): SecretKey {
        return SecretKeySpec(encoded, type.toSignatureAlgorithm())
    }

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