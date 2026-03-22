package org.whiteprint.platform.core.kms.model

import java.time.Instant

data class KeyMetadata(
    val keyId: KeyId,
    val type: KeyType,
    val status: KeyStatus = KeyStatus.ENABLED,
    val createdAt: Instant,
    val expiresAt: Instant? = null
)