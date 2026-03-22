package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMetadata
import org.whiteprint.platform.core.kms.model.KeyType

interface KeyAdminService {
    fun createKey(type: KeyType, alias: String? = null): KeyId
    fun rotateKey(keyId: KeyId): KeyId
    fun revokeKey(keyId: KeyId)
    fun getMetadata(keyId: KeyId): KeyMetadata
    fun findKeyIdByAlias(alias: String): KeyId?
}