package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.VaultTransitKey
import org.whiteprint.platform.core.kms.model.KeyBundle
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeyMetadata
import org.whiteprint.platform.core.kms.model.KeySide
import org.whiteprint.platform.core.kms.model.KeyStatus
import org.whiteprint.platform.core.kms.model.toKeyType
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Base64

class VaultKeyMaterialProvider(
    private val vaultOperations: VaultOperations,
    private val keyCache: KeyCache,
    private val transitPath: String = "transit"
) : KeyMaterialProvider {

    override fun getKeyBundle(
        keyId: KeyId,
        side: KeySide
    ): KeyBundle {
        keyCache.getBundle(keyId, side)?.let { return it }
        val bundle = fetchFromVault(keyId, side)
        keyCache.putBundle(side, bundle)
        return bundle
    }

    private fun fetchFromVault(keyId: KeyId, side: KeySide): KeyBundle {
        val response = vaultOperations.opsForTransit(transitPath).getKey(keyId.alias)
            ?: throw KmsException(
                policy = KmsPolicy.KMS_EXTERNAL_ERROR,
                attributes = mapOf(
                    "reason" to "Key not found in Vault: ${keyId.alias}"
                )
            )

        val metadata = mapToMetadata(keyId, response)

        val material = when (side) {
            KeySide.PUBLIC -> extractPublicKey(keyId, response)
            KeySide.SECRET -> extractSecretKey(keyId, response)
            KeySide.PRIVATE -> throw KmsException(
                policy = KmsPolicy.KMS_NOT_SUPPORTED,
                attributes = mapOf(
                    "reason" to "Private key export is not allowed"
                )
            )
        }

        return KeyBundle(material, metadata)
    }

    private fun mapToMetadata(keyId: KeyId, response: VaultTransitKey): KeyMetadata {
        val latestVersion = response.latestVersion.toString()
        val targetVersion = keyId.version ?: latestVersion

        val versionInfo = response.keys[targetVersion] as? Map<String, Any> ?: emptyMap()

        val createdAt = (versionInfo["creation_time"] as? String)?.let {
            OffsetDateTime.parse(it).toInstant()
        } ?: Instant.EPOCH

        return KeyMetadata(
            keyId = KeyId(keyId.alias, targetVersion),
            type = response.type.toKeyType(),
            status = resolveStatus(response),

            createdAt = createdAt,
            updatedAt = createdAt,

            latestVersion = latestVersion,
            minAvailableVersion = response.minDecryptionVersion.toString(),

                        isExportable = response.isExportable,
            isDeletable = response.isDeletionAllowed,

            expiresAt = null,
            tags = emptyMap()
        )
    }

    private fun resolveStatus(response: VaultTransitKey): KeyStatus {
        if (response.isDeletionAllowed) {
            return KeyStatus.PENDING_DELETION
        }

        if (response.minEncryptionVersion > response.latestVersion) {
            return KeyStatus.DISABLED
        }

        if (response.minDecryptionVersion > response.latestVersion) {
            return KeyStatus.EXPIRED
        }

        return KeyStatus.ENABLED
    }

    private fun extractPublicKey(keyId: KeyId, response: VaultTransitKey): KeyMaterial {
        val targetVersion = keyId.version ?: response.latestVersion.toString()

        val versionData = response.keys[targetVersion] as? Map<*, *>
            ?: throw KmsException(
                policy = KmsPolicy.KMS_EXTERNAL_ERROR,
                attributes = mapOf("reason" to "Version $targetVersion not found for key ${keyId.alias}")
            )

        val pemContent = versionData["public_key"] as? String
            ?: throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf("reason" to "Public key material missing for version $targetVersion")
            )

        return KeyMaterial(
            keyId = KeyId(keyId.alias, targetVersion),
            type = response.type.toKeyType(),
            side = KeySide.PUBLIC,
            encoded = decodePemToDer(pemContent)
        )
    }

    private fun extractSecretKey(keyId: KeyId, response: VaultTransitKey): KeyMaterial {
        if (!response.isExportable) {
            throw KmsException(
                policy = KmsPolicy.KMS_NOT_SUPPORTED,
                attributes = mapOf(
                    "alias" to keyId.alias,
                    "reason" to "Key is not exportable. Check Vault transit key configuration."
                )
            )
        }

        val targetVersion = keyId.version ?: response.latestVersion.toString()

        val versionData = response.keys[targetVersion] as? Map<*, *>
            ?: throw KmsException(
                policy = KmsPolicy.KMS_EXTERNAL_ERROR,
                attributes = mapOf("reason" to "Version $targetVersion not found for key ${keyId.alias}")
            )

        val rawSecret = versionData["key"] as? String
            ?: throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf(
                    "alias" to keyId.alias,
                    "version" to targetVersion,
                    "reason" to "Secret key material is missing in response despite exportable=true"
                )
            )

        return KeyMaterial(
            keyId = KeyId(keyId.alias, targetVersion),
            type = response.type.toKeyType(),
            side = KeySide.SECRET,
            encoded = try {
                Base64.getDecoder().decode(rawSecret)
            } catch (e: IllegalArgumentException) {
                throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Invalid Base64 in secret key"))
            }
        )
    }

    private fun decodePemToDer(pem: String): ByteArray {
        val cleanPem = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        return try {
            Base64.getDecoder().decode(cleanPem)
        } catch (e: IllegalArgumentException) {
            throw KmsException(
                policy = KmsPolicy.KMS_EXTERNAL_ERROR,
                attributes = mapOf("reason" to "Invalid Base64 encoding in public key")
            )
        }
    }

}