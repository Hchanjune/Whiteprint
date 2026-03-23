package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Ciphertext
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import org.whiteprint.platform.core.kms.model.EncryptionResult
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.SigningResult
import org.whiteprint.platform.core.kms.service.KeyOperations
import java.util.Base64

class VaultKeyOperations(
    private val vaultOperations: VaultOperations,
    private val transitPath: String = "transit"
) : KeyOperations {

    override fun sign(alias: String, data: ByteArray): SigningResult {
        val ops = vaultOperations.opsForTransit(transitPath)

        val plaintext = Plaintext.of(data)

        val vaultSignature: Signature = ops.sign(alias, plaintext)
        val rawSignature = vaultSignature.signature // "vault:v1:..."

        val parts = rawSignature.split(":")
        val version = parts[1].removePrefix("v")
        val signatureBytes = Base64.getDecoder().decode(parts[2])

        return SigningResult(
            keyId = KeyId(alias, version),
            signature = signatureBytes
        )
    }

    override fun verify(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean {
        val ops = vaultOperations.opsForTransit(transitPath)

        val plaintext = Plaintext.of(data)
        val vaultFormat = "vault:v${keyId.version}:${Base64.getEncoder().encodeToString(signature)}"
        val vaultSignature = Signature.of(vaultFormat)

        return ops.verify(keyId.alias, plaintext, vaultSignature)
    }

    override fun encrypt(alias: String, plainText: ByteArray): EncryptionResult {
        val ops = vaultOperations.opsForTransit(transitPath)

        val plaintext = Plaintext.of(plainText)
        val vaultCipherText: Ciphertext = ops.encrypt(alias, plaintext)
        val rawCipher = vaultCipherText.ciphertext // "vault:v1:..."

        val parts = rawCipher.split(":")
        val version = parts[1].removePrefix("v")
        val cipherBytes = Base64.getDecoder().decode(parts[2])

        return EncryptionResult(
            keyId = KeyId(alias, version),
            cipherText = cipherBytes,
            iv = null
        )
    }

    override fun decrypt(keyId: KeyId, cipherText: ByteArray): ByteArray {
        val ops = vaultOperations.opsForTransit(transitPath)

        val vaultFormat = "vault:v${keyId.version}:${Base64.getEncoder().encodeToString(cipherText)}"
        val ciphertext = Ciphertext.of(vaultFormat)

        val plaintext: Plaintext = ops.decrypt(keyId.alias, ciphertext)
        return plaintext.plaintext
    }

}