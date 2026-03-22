package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeyMetadata

interface KeyCache {
    fun getMetadata(keyId: KeyId): KeyMetadata?
    fun putMetadata(metadata: KeyMetadata)

    fun getPublicKey(keyId: KeyId): KeyMaterial?
    fun putPublicKey(keyId: KeyId, material: KeyMaterial)
}