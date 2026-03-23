package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyBundle
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMetadata
import org.whiteprint.platform.core.kms.model.KeyStatus
import org.whiteprint.platform.core.kms.model.KeyType

interface KeyAdminOperations {
    fun createKey(alias: String, type: KeyType): KeyBundle
    fun rotateKey(alias: String): KeyBundle
    fun updateStatus(keyId: KeyId, status: KeyStatus)
    fun getBundle(alias: String): KeyBundle
    fun findLatestKeyId(alias: String): KeyId?
    fun findAllVersions(alias: String): List<KeyId>
}