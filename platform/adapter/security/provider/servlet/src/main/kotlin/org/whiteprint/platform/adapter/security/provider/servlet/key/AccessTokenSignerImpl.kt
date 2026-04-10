package org.whiteprint.platform.adapter.security.provider.servlet.key

import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKmsConfigurationProperties
import org.whiteprint.platform.core.kms.model.toJjwtAlgorithm
import org.whiteprint.platform.core.kms.policy.KmsException
import org.whiteprint.platform.core.kms.policy.KmsPolicy
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.model.AccessTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.AccessTokenSigningResult
import org.whiteprint.platform.core.security.provider.AccessTokenSigner

class AccessTokenSignerImpl(
    private val keyOperations: KeyOperations,
    private val adminOperations: KeyAdminOperations,
    private val accessTokenPolicy: SecurityProviderKmsConfigurationProperties.AccessTokenKeyPolicy
): AccessTokenSigner {

    override fun getLatestSigningKeyMetadata(): AccessTokenSigningKeyMetadata {
        val keyId = adminOperations.findLatestKeyId(accessTokenPolicy.keyAlias)
            ?: throw KmsException(
                policy = KmsPolicy.KEY_NOT_FOUND,
                attributes = mapOf(
                    "keyId" to accessTokenPolicy.keyAlias,
                )
            )

        return AccessTokenSigningKeyMetadata(
            keyAlias = keyId.alias,
            keyVersion = keyId.version
                ?: throw KmsException(
                    policy = KmsPolicy.KEY_NOT_FOUND,
                    attributes = mapOf(
                        "keyId" to accessTokenPolicy.keyAlias,
                    )
                ),
            algorithm = accessTokenPolicy.algorithm.toJjwtAlgorithm()
        )
    }

    override fun sign(data: ByteArray): AccessTokenSigningResult {
        val signedResult = keyOperations.sign(accessTokenPolicy.keyAlias, data)
        return AccessTokenSigningResult(
            keyAlias = signedResult.keyId.alias,
            keyVersion = signedResult.keyId.version,
            algorithm = accessTokenPolicy.algorithm.toJjwtAlgorithm(),
            signature = signedResult.signature,
        )
    }

}