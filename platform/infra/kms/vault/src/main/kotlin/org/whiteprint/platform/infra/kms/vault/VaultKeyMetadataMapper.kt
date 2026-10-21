package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.support.VaultTransitKey
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMetadata
import org.whiteprint.platform.core.kms.model.KeyStatus
import org.whiteprint.platform.core.kms.model.toKeyType
import java.time.Instant
import java.time.OffsetDateTime

/** Vault transit 키 응답 → [KeyMetadata]. [keyId] 의 version 이 null 이면 최신 버전 기준이다. */
internal fun VaultTransitKey.toKeyMetadata(keyId: KeyId): KeyMetadata {
    val latestVersion = latestVersion.toString()
    val targetVersion = keyId.version ?: latestVersion

    val versionInfo = keys[targetVersion] as? Map<*, *> ?: emptyMap<String, Any>()

    val createdAt = (versionInfo["creation_time"] as? String)?.let {
        OffsetDateTime.parse(it).toInstant()
    } ?: Instant.EPOCH

    return KeyMetadata(
        keyId = KeyId(keyId.alias, targetVersion),
        type = type.toKeyType(),
        status = resolveStatus(),

        createdAt = createdAt,
        updatedAt = createdAt,

        latestVersion = latestVersion,
        minAvailableVersion = minDecryptionVersion.toString(),

        isExportable = isExportable,
        isDeletable = isDeletionAllowed,

        expiresAt = null,
        tags = emptyMap()
    )
}

private fun VaultTransitKey.resolveStatus(): KeyStatus {
    if (isDeletionAllowed) {
        return KeyStatus.PENDING_DELETION
    }

    if (minEncryptionVersion > latestVersion) {
        return KeyStatus.DISABLED
    }

    if (minDecryptionVersion > latestVersion) {
        return KeyStatus.EXPIRED
    }

    return KeyStatus.ENABLED
}
