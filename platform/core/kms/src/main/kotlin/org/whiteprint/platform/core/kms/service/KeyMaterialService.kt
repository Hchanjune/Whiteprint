package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial

interface KeyMaterialService {

    fun getPublicKey(keyId: KeyId): KeyMaterial

    fun getPrivateKey(keyId: KeyId): KeyMaterial

    fun getSecretKey(keyId: KeyId): KeyMaterial

}