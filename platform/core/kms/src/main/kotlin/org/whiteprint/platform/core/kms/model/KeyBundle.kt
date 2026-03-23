package org.whiteprint.platform.core.kms.model

import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy

data class KeyBundle(
    val material: KeyMaterial?,
    val metadata: KeyMetadata,
) {
    fun getRequiredMaterial(): KeyMaterial {
        return material?: throw KmsException(
            policy = KmsPolicy.MATERIAL_NOT_FOUND,
            attributes = mapOf(
                "alias" to metadata.keyId.alias,
                "version" to metadata.keyId.version,
            )
        )
    }
}
