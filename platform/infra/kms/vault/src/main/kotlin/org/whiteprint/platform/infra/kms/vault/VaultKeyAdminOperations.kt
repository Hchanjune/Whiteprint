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

        val request = mutableMapOf<String, Any>(
            "type" to type.toVaultType(),
            "exportable" to true,
            "deletion_allowed" to true
        )

        vaultOperations.write(path, request)

        return getBundle(alias)
    }

    override fun rotateKey(alias: String): KeyBundle {
        val ops = vaultOperations.opsForTransit(transitPath)
        ops.rotate(alias)
        return getBundle(alias)
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

    override fun findAllVersions(alias: String): List<KeyId> {
        val key = vaultOperations.opsForTransit(transitPath).getKey(alias)
            ?: return emptyList()
        return key.keys.keys.map { version -> KeyId(alias, version) }
    }


}