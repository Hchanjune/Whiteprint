package org.whiteprint.platform.core.kms.model

import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy

data class KeyId(
    val alias: String,
    val version: String
) {
    override fun toString(): String = "$alias:$version"

    companion object {
        fun from(value: String): KeyId {
            val parts = value.split(":")
            if (parts.size != 2) {
                throw KmsException(
                    policy = KmsPolicy.INVALID_KEY_ID_FORMAT,
                    attributes = mapOf(
                        "input" to value,
                    )
                )
            }
            return KeyId(parts[0], parts[1])
        }
    }

}