package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeyType
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyMaterialService

class VaultKeyMaterialService(
    private val vaultOperations: VaultOperations
) : KeyMaterialService {

    /**
     * Vault Transit 키 정보를 조회하여 공개키(Public Key)를 추출합니다.
     * GET /transit/keys/{keyId}
     */
    override fun getPublicKey(keyId: KeyId): KeyMaterial {
        val response = vaultOperations.read("transit/keys/${keyId.value}")
            ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        val data = response.data ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        // Vault 응답 구조에서 'keys' 맵의 최신 버전을 찾습니다.
        val keys = data["keys"] as? Map<String, Any>
        val latestVersionKey = keys?.values?.lastOrNull() as? Map<String, Any>

        val publicKeyString = latestVersionKey?.get("public_key") as? String
            ?: throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Public key not found for key: ${keyId.value}"))

        return KeyMaterial(
            keyId = keyId,
            type = KeyType.RSA_2048, // 실제로는 data["type"]을 파싱하여 매핑해야 함
            encoded = publicKeyString.toByteArray()
        )
    }

    /**
     * Transit Engine은 기본적으로 개인키 내보내기를 지원하지 않습니다.
     * 보안 정책상 이 기능을 차단하거나, 반드시 필요한 경우 별도의 로직을 타야 합니다.
     */
    override fun getFullKeyMaterial(keyId: KeyId): KeyMaterial {
        throw UnsupportedOperationException(
            "Exporting full key material (Private Key) is disabled by Vault Transit policy to ensure hardware-level security."
        )
    }
}