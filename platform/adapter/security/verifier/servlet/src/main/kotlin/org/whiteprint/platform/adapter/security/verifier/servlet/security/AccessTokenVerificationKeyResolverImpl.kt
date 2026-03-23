package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.toJavaAlgorithm
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.model.AccessTokenVerificationKey
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver

class AccessTokenVerificationKeyResolverImpl(
    private val keyCache: KeyCache,
    private val keyMaterialProvider: KeyMaterialProvider
): AccessTokenVerificationKeyResolver {

    override fun resolve(keyId: String): AccessTokenVerificationKey {
        val kid = KeyId(keyId)

        val material = keyCache.getPublicKey(kid)
            ?: keyMaterialProvider.getPublicKey(kid).also { keyCache.putPublicKey(kid, it) }

        val publicKey = try {
            val algorithm = material.type.toJavaAlgorithm()
            java.security.KeyFactory.getInstance(algorithm)
                .generatePublic(java.security.spec.X509EncodedKeySpec(material.encoded))
        } catch (exception: Exception) {
            throw KmsException(
                policy = KmsPolicy.KMS_EXTERNAL_ERROR,
                attributes = mapOf(
                    "reason" to "Failed to generate Public Key. Check if key type is asymmetric. current input : ${material.type.name}",
                ),
                cause = exception
            )
        }

        return AccessTokenVerificationKey(
            keyId = keyId,
            verifyKey = publicKey as java.security.PublicKey
        )
    }

}