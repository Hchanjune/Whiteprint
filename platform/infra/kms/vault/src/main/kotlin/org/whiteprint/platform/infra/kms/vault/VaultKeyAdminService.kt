package org.whiteprint.platform.infra.kms.vault.adapter

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.VaultTransitKeyConfiguration
import org.whiteprint.platform.core.kms.model.*
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyAdminService
import java.time.Instant

class VaultKeyAdminService(
    private val vaultOperations: VaultOperations
) : KeyAdminService {

    private val transit = vaultOperations.opsForTransit()

    override fun createKey(type: KeyType, alias: String?): KeyId {
        val keyId = alias ?: "k-${System.currentTimeMillis()}"

        // configuration 객체 없이, 필요한 'type' 정보만 Map에 담아 Vault에 직접 요청합니다.
        // POST /transit/keys/{keyId} { "type": "{vaultType}" }
        val requestPath = "transit/keys/$keyId"
        val body = mapOf("type" to type.toVaultType())

        vaultOperations.write(requestPath, body)

        return KeyId(keyId)
    }

    override fun getMetadata(keyId: KeyId): KeyMetadata {
        val vaultKey = transit.getKey(keyId.value)
            ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        return KeyMetadata(
            keyId = keyId,
            type = vaultKey.type.toKeyType(),
            status = if (vaultKey.isDeletionAllowed) KeyStatus.PENDING_DELETION else KeyStatus.ENABLED,
            createdAt = Instant.ofEpochSecond(vaultKey.latestVersion.toLong()), // 버전 정보를 시점으로 활용
            expiresAt = null
        )
    }

    override fun rotateKey(keyId: KeyId): KeyId {
        transit.rotate(keyId.value)
        return keyId
    }

    override fun revokeKey(keyId: KeyId) {
        // Vault 정책상 삭제 허용 설정 후 제거
        vaultOperations.write("transit/keys/${keyId.value}/config", mapOf("deletion_allowed" to true))
        vaultOperations.delete("transit/keys/${keyId.value}")
    }

    override fun findKeyIdByAlias(alias: String): KeyId? {
        return try {
            // 키 존재 여부 확인만 수행
            if (transit.getKey(alias) != null) KeyId(alias) else null
        } catch (e: Exception) {
            null
        }
    }

    // --- 내부 확장 함수: 알고리즘 매핑 ---

    private fun KeyType.toVaultType(): String = when (this) {
        KeyType.RSA_2048 -> "rsa-2048"
        KeyType.RSA_4096 -> "rsa-4096"
        KeyType.EC_P256 -> "ecdsa-p256"
        KeyType.AES_128_GCM -> "aes128-gcm96"
        KeyType.AES_256_GCM -> "aes256-gcm96"
        KeyType.HMAC_SHA256 -> "hmac"
    }

    private fun String.toKeyType(): KeyType = when (this) {
        "rsa-2048" -> KeyType.RSA_2048
        "rsa-4096" -> KeyType.RSA_4096
        "ecdsa-p256" -> KeyType.EC_P256
        "aes128-gcm96" -> KeyType.AES_128_GCM
        "aes256-gcm96" -> KeyType.AES_256_GCM
        "hmac" -> KeyType.HMAC_SHA256
        else -> throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Unknown vault key type: $this"))
    }
}