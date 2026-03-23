package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.toKeyType
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider

class VaultKeyMaterialProvider(
    private val vaultOperations: VaultOperations
) : KeyMaterialProvider {

    /**
     * Vault Transit 키 정보를 조회하여 공개키(Public Key)를 추출합니다.
     * GET /transit/keys/{keyId}
     */
    override fun getPublicKey(keyId: KeyId): KeyMaterial {
        val response = vaultOperations.read("transit/keys/${keyId.value}")
            ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        val data = response.data ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        // 1. 명시적인 최신 버전 번호 추출
        val latestVersion = data["latest_version"]?.toString()
            ?: throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Latest version not found"))

        // 2. 해당 버전의 데이터 맵 추출
        val keys = data["keys"] as? Map<String, Any>
        val latestKeyData = keys?.get(latestVersion) as? Map<String, Any>

        val pemString = latestKeyData?.get("public_key") as? String
            ?: throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Public key not found"))

        // 3. PEM 문자열에서 순수 키 자재(DER) 추출 (정규식 등으로 Header/Footer 제거 및 Base64 디코딩)
        val encodedBytes = extractDerFromPem(pemString)

        return KeyMaterial(
            keyId = keyId,
            type = (data["type"] as String).toKeyType(),
            encoded = encodedBytes
        )
    }

    override fun getLatestPublicKey(alias: String): KeyMaterial {
        TODO("Not yet implemented")
    }

    private fun extractDerFromPem(pem: String): ByteArray {
        val cleanPem = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "") // 줄바꿈 및 공백 제거
        return java.util.Base64.getDecoder().decode(cleanPem)
    }

    /**
     * Transit Engine은 기본적으로 개인키 내보내기를 지원하지 않습니다.
     * 보안 정책상 이 기능을 차단하거나, 반드시 필요한 경우 별도의 로직을 타야 합니다.
     */
    override fun getPrivateKey(keyId: KeyId): KeyMaterial {
        throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "Exporting full key material (Private Key) is disabled by Vault Transit policy to ensure hardware-level security. for key: ${keyId.value}"))
    }

    override fun getLatestPrivateKey(alias: String): KeyMaterial {
        TODO("Not yet implemented")
    }

    override fun getSecretKey(keyId: KeyId): KeyMaterial {
        // 1. Vault Transit Export 엔드포인트 호출 (HMAC/대칭키 자재 추출)
        // 경로: transit/export/signing-key/{keyId} 또는 encryption-key/{keyId}
        val response = vaultOperations.read("transit/export/signing-key/${keyId.value}")
            ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        val data = response.data ?: throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.value))

        // 2. keys 맵에서 키 자재 추출 (Vault는 보통 Base64 문자열로 반환)
        val keys = data["keys"] as? Map<String, String>
        val rawKeyBase64 = keys?.values?.firstOrNull()
            ?: throw KmsException(KmsPolicy.KMS_EXTERNAL_ERROR, mapOf("reason" to "SecretKey not found for key: ${keyId.value}"))

        // 3. Base64 디코딩 후 KeyMaterial 생성
        return KeyMaterial(
            keyId = keyId,
            type = (data["type"] as String).toKeyType(),
            encoded = java.util.Base64.getDecoder().decode(rawKeyBase64)
        )
    }

    override fun getLatestSecretKey(alias: String): KeyMaterial {
        TODO("Not yet implemented")
    }


}