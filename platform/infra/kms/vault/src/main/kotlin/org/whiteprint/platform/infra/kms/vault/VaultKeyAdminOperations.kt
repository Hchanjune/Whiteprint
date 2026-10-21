package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.VaultTransitKeyConfiguration
import org.whiteprint.platform.core.kms.model.*
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import java.time.Instant

class VaultKeyAdminOperations(
    private val vaultOperations: VaultOperations,
    private val keyMaterialProvider: KeyMaterialProvider,
    private val transitPath: String
) : KeyAdminOperations {

    override fun createKey(alias: String, type: KeyType): KeyBundle {
        val path = "$transitPath/keys/$alias"

        // 내보내기는 키 종류가 허용하는 만큼만(비대칭 개인키는 안 됨), 삭제는 막는다 — 지우면 그 키로 만든 서명·암호문이 전부 죽는다.
        val request = mutableMapOf<String, Any>(
            "type" to type.toVaultType(),
            "exportable" to type.isExportable,
            "deletion_allowed" to false
        )

        vaultOperations.write(path, request)

        return latestBundle(alias)
    }

    override fun rotateKey(alias: String): KeyBundle {
        val ops = vaultOperations.opsForTransit(transitPath)
        ops.rotate(alias)
        return latestBundle(alias)
    }

    override fun updateStatus(keyId: KeyId, status: KeyStatus) {
        val path = "$transitPath/config/${keyId.alias}"

        val config = when (status) {
            KeyStatus.DISABLED -> {
                val latest = findLatestKeyId(keyId.alias)?.version?.toInt() ?: 1
                VaultTransitKeyConfiguration.builder()
                    .minEncryptionVersion(latest + 1)
                    .build()
            }
            KeyStatus.ENABLED -> {
                VaultTransitKeyConfiguration.builder()
                    .minEncryptionVersion(0)
                    .minDecryptionVersion(0)
                    .build()
            }
            else -> throw KmsException(KmsPolicy.KMS_NOT_SUPPORTED)
        }

        vaultOperations.write(path, config)
    }

    override fun getBundle(alias: String): KeyBundle {
        return keyMaterialProvider.getKeyBundle(KeyId(alias, null), KeySide.PUBLIC)
    }

    override fun findLatestKeyId(alias: String): KeyId? {
        val key = vaultOperations.opsForTransit(transitPath).getKey(alias)
        return key?.let { KeyId(alias, it.latestVersion.toString()) }
    }

    override fun findMetadata(alias: String): KeyMetadata? =
        vaultOperations.opsForTransit(transitPath).getKey(alias)
            ?.toKeyMetadata(KeyId(alias, null))

    /** 공개키가 있는 키는 공개키까지, 대칭키는 메타데이터만 — [getBundle] 은 공개키가 없으면 실패한다. */
    private fun latestBundle(alias: String): KeyBundle {
        val metadata = findMetadata(alias)
            ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to alias))
        if (!metadata.type.isAsymmetric) {
            return KeyBundle(material = null, metadata = metadata)
        }
        return keyMaterialProvider.getKeyBundle(metadata.keyId, KeySide.PUBLIC)
    }

    override fun findAllVersions(alias: String): List<KeyId> {
        val key = vaultOperations.opsForTransit(transitPath).getKey(alias)
            ?: return emptyList()
        return key.keys.keys.map { version -> KeyId(alias, version) }
    }


}