package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeyMetadata
import java.security.PrivateKey

interface KeyCache {
    fun getMetadata(keyId: KeyId): KeyMetadata?
    fun putMetadata(metadata: KeyMetadata)

    fun getPublicKey(keyId: KeyId): KeyMaterial?
    fun putPublicKey(keyId: KeyId, material: KeyMaterial)

    fun getPrivateKey(keyId: KeyId): KeyMaterial?
    fun putPrivateKey(keyId: KeyId, material: KeyMaterial)

    fun getSecretKey(keyId: KeyId): KeyMaterial?
    fun putSecretKey(keyId: KeyId, material: KeyMaterial)
}