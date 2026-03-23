package org.whiteprint.platform.core.kms.model

import java.time.Instant

data class KeyMetadata(
    val keyId: KeyId,
    val type: KeyType,
    val status: KeyStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val latestVersion: String,
    val minAvailableVersion: String,
    val isExportable: Boolean,
    val isDeletable: Boolean,
    val expiresAt: Instant?,
    val tags: Map<String, String>
)