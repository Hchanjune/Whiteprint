package org.whiteprint.platform.adapter.security.provider.servlet.key

import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKmsConfigurationProperties
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeySide
import org.whiteprint.platform.core.kms.model.toJjwtAlgorithm
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.model.RefreshTokenKey
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver

class RefreshTokenKeyResolverImpl(
    private val keyMaterialProvider: KeyMaterialProvider,
    private val refreshTokenPolicy: SecurityProviderKmsConfigurationProperties.RefreshTokenKeyPolicy
): RefreshTokenKeyResolver {

    override fun resolve(): RefreshTokenKey {
        val bundle = keyMaterialProvider.getKeyBundle(
            keyId = KeyId(refreshTokenPolicy.keyAlias, null),
            side = KeySide.SECRET
        )

        val secretKey = bundle.material?.toSecretKey()?:
            throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf(
                    "keyId" to refreshTokenPolicy.keyAlias,
                )
            )

        return RefreshTokenKey(
            keyId = bundle.metadata.keyId.toString(),
            secretKey = secretKey,
            algorithm = refreshTokenPolicy.algorithm.toJjwtAlgorithm()
        )

    }
}