package org.whiteprint.platform.adapter.security.provider.servlet.key

import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKmsConfigurationProperties
import org.whiteprint.platform.core.kms.model.toJwtAlgorithm
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.model.RefreshTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.RefreshTokenSigningResult
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner

class RefreshTokenSignerImpl(
    private val keyOperations: KeyOperations,
    private val adminOperations: KeyAdminOperations,
    private val refreshTokenPolicy: SecurityProviderKmsConfigurationProperties.RefreshTokenKeyPolicy
): RefreshTokenSigner {

    override fun getLatestSigningKeyMetadata(): RefreshTokenSigningKeyMetadata {
        val keyId = adminOperations.findLatestKeyId(refreshTokenPolicy.keyAlias)
            ?: throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf(
                    "keyId" to refreshTokenPolicy.keyAlias,
                )
            )
        return RefreshTokenSigningKeyMetadata(
            keyAlias = keyId.alias,
            keyVersion = keyId.version
                ?: throw KmsException(
                    policy = KmsPolicy.KEY_NOT_FOUND,
                    attributes = mapOf(
                        "keyId" to refreshTokenPolicy.keyAlias,
                    )
                ),
            algorithm = refreshTokenPolicy.algorithm.toJwtAlgorithm()
        )
    }

    override fun sign(text: String): RefreshTokenSigningResult {
        val signingResult = keyOperations.sign(refreshTokenPolicy.keyAlias, text)
        return RefreshTokenSigningResult(
            keyAlias = signingResult.keyId.alias,
            keyVersion = signingResult.keyId.version,
            algorithm = refreshTokenPolicy.algorithm.toJwtAlgorithm(),
            signature = signingResult.signature
        )
    }
}