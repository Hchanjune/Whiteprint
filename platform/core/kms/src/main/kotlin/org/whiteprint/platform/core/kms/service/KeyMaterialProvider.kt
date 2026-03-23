package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyBundle
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeySide

interface KeyMaterialProvider {

    fun getKeyBundle(keyId: KeyId, side: KeySide): KeyBundle
    fun getPublicKey(keyId: KeyId): KeyMaterial = getKeyBundle(keyId, KeySide.PUBLIC).getRequiredMaterial()
    fun getSecretKey(keyId: KeyId): KeyMaterial = getKeyBundle(keyId, KeySide.SECRET).getRequiredMaterial()

}