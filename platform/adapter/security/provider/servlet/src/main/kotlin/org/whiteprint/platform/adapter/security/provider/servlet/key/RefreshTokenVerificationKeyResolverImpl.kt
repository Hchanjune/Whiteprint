package org.whiteprint.platform.adapter.security.provider.servlet.key

import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeySide
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.model.RefreshTokenVerificationKey
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerificationKeyResolver

class RefreshTokenVerificationKeyResolverImpl(
    private val keyMaterialProvider: KeyMaterialProvider,
): RefreshTokenVerificationKeyResolver {

    override fun resolve(keyAlias: String, keyVersion: String?): RefreshTokenVerificationKey {
        val bundle = keyMaterialProvider.getKeyBundle(
            keyId = KeyId(keyAlias, keyVersion),
            side = KeySide.PUBLIC
        )

        val publicKey = bundle.material?.toPublicKey()
            ?: throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf(
                    "keyId" to keyAlias,
                    "version" to keyVersion.toString()
                )
            )

        return RefreshTokenVerificationKey(
            keyAlias = keyAlias,
            keyVersion = keyVersion,
            verifyKey = publicKey,
        )
    }

}