package org.whiteprint.platform.adapter.security.provider.servlet.key

import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKmsConfigurationProperties
import org.whiteprint.platform.core.kms.model.toJjwtAlgorithm
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.provider.AccessTokenSigner

class AccessTokenSignerImpl(
    private val keyOperations: KeyOperations,
    private val accessTokenPolicy: SecurityProviderKmsConfigurationProperties.AccessTokenKeyPolicy
): AccessTokenSigner {

    override fun getKeyId(): String = accessTokenPolicy.keyAlias

    override fun getAlgorithm(): String = accessTokenPolicy.algorithm.toJjwtAlgorithm()

    override fun sign(data: ByteArray): ByteArray = keyOperations.sign(accessTokenPolicy.keyAlias, data).signature
}