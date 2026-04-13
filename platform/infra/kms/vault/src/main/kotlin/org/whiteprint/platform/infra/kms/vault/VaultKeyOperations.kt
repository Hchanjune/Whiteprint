package org.whiteprint.platform.infra.kms.vault

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Ciphertext
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.whiteprint.platform.core.kms.model.EncryptionResult
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.SigningResult
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyOperations
import java.util.Base64

class VaultKeyOperations(
    private val vaultOperations: VaultOperations,
    private val transitPath: String
) : KeyOperations {

    /**
     * RS256 (pkcs1v15)
     */
    override fun sign(keyAlias: String, rawText: String): SigningResult {
        val base64Input = Base64.getEncoder()
            .encodeToString(rawText.toByteArray(Charsets.UTF_8))

        val response = try {
            vaultOperations.write(
                "$transitPath/sign/$keyAlias",
                mapOf(
                    "input" to base64Input,
                    "signature_algorithm" to "pkcs1v15",
                )
            )
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyAlias), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }

        val vaultSig = response?.data?.get("signature") as? String
            ?: throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap())

        val parts = vaultSig.split(":")
        val version = parts[1].removePrefix("v")
        val signatureBytes = Base64.getDecoder().decode(parts[2].trim())

        return SigningResult(
            keyId = KeyId(keyAlias, version),
            signature = signatureBytes
        )
    }

    /**
     * RS256 (pkcs1v15)
     */
    override fun verify(keyId: KeyId, rawText: String, signature: ByteArray): Boolean {
        val base64Input = Base64.getEncoder()
            .encodeToString(rawText.toByteArray(Charsets.UTF_8))
        val vaultSig = "vault:v${keyId.version}:${Base64.getEncoder().encodeToString(signature)}"

        val response = try {
            vaultOperations.write(
                "$transitPath/verify/${keyId.alias}",
                mapOf(
                    "input" to base64Input,
                    "signature" to vaultSig,
                    "signature_algorithm" to "pkcs1v15",
                )
            )
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.toString()), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }

        return response?.data?.get("valid") as? Boolean
            ?: throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap())
    }

    /**
     * Signs binary data using RSA-PSS (salt=MAX_LENGTH).
     * Verification must be done via Vault only, as the salt length
     * is incompatible with standard JWT libraries (e.g., jwt.io expects salt=32).
     */
    override fun signBinary(keyAlias: String, data: ByteArray): SigningResult {
        val ops = vaultOperations.opsForTransit(transitPath)
        val plaintext = Plaintext.of(data)

        val vaultSignature = try {
            ops.sign(keyAlias, plaintext)
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyAlias), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }

        val parts = vaultSignature.signature.split(":")
        val version = parts[1].removePrefix("v")
        val signatureBytes = Base64.getDecoder().decode(parts[2].trim())

        return SigningResult(
            keyId = KeyId(keyAlias, version),
            signature = signatureBytes
        )
    }

    /**
     * Verifies binary data using RSA-PSS (salt=MAX_LENGTH).
     * Signing must be done via Vault only, as the salt length
     * is incompatible with standard JWT libraries (e.g., jwt.io expects salt=32).
     */
    override fun verifyBinary(keyId: KeyId, data: ByteArray, signature: ByteArray): Boolean {
        val ops = vaultOperations.opsForTransit(transitPath)

        val plaintext = Plaintext.of(data)
        val vaultFormat = "vault:v${keyId.version}:${Base64.getEncoder().encodeToString(signature)}"
        val vaultSignature = Signature.of(vaultFormat)

        return try {
            ops.verify(keyId.alias, plaintext, vaultSignature)
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.toString()), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }
    }

    override fun encrypt(alias: String, plainText: ByteArray): EncryptionResult {
        val ops = vaultOperations.opsForTransit(transitPath)

        val plaintext = Plaintext.of(plainText)
        val vaultCipherText: Ciphertext = try {
            ops.encrypt(alias, plaintext)
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to alias), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }
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

        val plaintext: Plaintext = try {
            ops.decrypt(keyId.alias, ciphertext)
        } catch (exception: HttpClientErrorException.NotFound) {
            throw KmsException(KmsPolicy.KEY_NOT_FOUND, mapOf("keyId" to keyId.toString()), exception)
        } catch (exception: HttpServerErrorException) {
            throw KmsException(KmsPolicy.KMS_INTERNAL_ERROR, emptyMap(), exception)
        }
        return plaintext.plaintext
    }

}