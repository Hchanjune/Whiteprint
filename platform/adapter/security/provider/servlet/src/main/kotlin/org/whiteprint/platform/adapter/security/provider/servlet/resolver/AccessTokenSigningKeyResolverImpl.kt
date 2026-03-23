package org.whiteprint.platform.adapter.security.provider.servlet.resolver

import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKmsConfigurationProperties
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.model.AccessTokenSigningKey
import org.whiteprint.platform.core.security.provider.AccessTokenSigningKeyResolver

class AccessTokenSigningKeyResolverImpl(
    private val keyMaterialProvider: KeyMaterialProvider,
    private val keyCache: KeyCache,
    private val properties: SecurityProviderKmsConfigurationProperties
): AccessTokenSigningKeyResolver {

    override fun resolve(): AccessTokenSigningKey {

    }

}