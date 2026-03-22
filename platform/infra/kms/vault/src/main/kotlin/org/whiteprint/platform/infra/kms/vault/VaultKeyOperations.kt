package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Ciphertext
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import org.springframework.vault.support.VaultTransitContext
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.service.KeyOperations

class VaultKeyOperations(
    private val vaultOperations: VaultOperations
) : KeyOperations {

    private val transit = vaultOperations.opsForTransit()

    override fun encrypt(keyId: KeyId, plainText: ByteArray, context: Map<String, String>?): ByteArray {
        // Plaintext.of(ByteArray)와 context를 조합
        val request = Plaintext.of(plainText).with(createContext(context))
        val response = transit.encrypt(keyId.value, request)

        // response는 Ciphertext 객체이므로 그 안의 문자열(vault:v1:...)을 추출
        return response.ciphertext.toByteArray()
    }

    override fun decrypt(keyId: KeyId, cipherText: ByteArray, context: Map<String, String>?): ByteArray {
        // Ciphertext.of(String)와 context 조합
        val request = Ciphertext.of(String(cipherText)).with(createContext(context))
        val response = transit.decrypt(keyId.value, request)

        return response.plaintext
    }

    override fun sign(keyId: KeyId, data: ByteArray): ByteArray {
        // Plaintext 객체로 래핑하여 전달
        val request = Plaintext.of(data)
        val response = transit.sign(keyId.value, request)

        // Signature 객체에서 문자열(vault:v1:...)을 추출하여 반환
        return response.signature.toByteArray()
    }

    override fun verify(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean {
        // Plaintext와 Signature 객체를 각각 생성하여 전달
        val plaintext = Plaintext.of(data)
        val signatureWrapper = Signature.of(String(signature))

        return transit.verify(keyId.value, plaintext, signatureWrapper)
    }

    private fun createContext(context: Map<String, String>?): VaultTransitContext {
        if (context.isNullOrEmpty()) return VaultTransitContext.empty()
        // 예: 맵을 바이트로 변환하여 컨텍스트화
        return VaultTransitContext.fromContext(context.toString().toByteArray())
    }
}