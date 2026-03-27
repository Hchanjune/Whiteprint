package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.adapter.security.provider.servlet.key.AccessTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenKeyResolverImpl
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.infra.kms.vault.VaultKeyAdminOperations
import org.whiteprint.platform.infra.kms.vault.CaffeineKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialProvider
import org.whiteprint.platform.infra.kms.vault.VaultKeyOperations

@Configuration
class SecurityProviderKmsConfiguration(
    private val kmsProperties: SecurityProviderKmsConfigurationProperties,
) {
    @Bean("providerVaultOperations")
    fun vaultOperations(): VaultOperations {
        val endpoint = VaultEndpoint.create(
            kmsProperties.datasource.host,
            kmsProperties.datasource.port,
        ).apply {
            this.scheme = "http"
        }

        val clientAuthentication = TokenAuthentication(kmsProperties.datasource.password)

        return VaultTemplate(endpoint, clientAuthentication)
    }

    @Bean("providerKeyOperations")
    fun keyOperations(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
    ): KeyOperations =
        VaultKeyOperations(
            vaultOperations = vaultOperations,
            transitPath = kmsProperties.datasource.transitPath
        )

    @Bean("providerKmsCache")
    fun securityKmsCache(): KeyCache =
        CaffeineKeyCache()

    @Bean("providerKeyMaterialProvider")
    fun securityKeyMaterialService(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
        @Qualifier("providerKmsCache") keyCache: KeyCache,
    ): KeyMaterialProvider =
        VaultKeyMaterialProvider(
            vaultOperations = vaultOperations,
            keyCache = keyCache,
            transitPath = kmsProperties.datasource.transitPath,
        )

    @Bean("providerKeyAdminService")
    fun securityKeyAdminService(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
        @Qualifier("providerKeyMaterialProvider") keyMaterialProvider: KeyMaterialProvider,
    ): KeyAdminOperations =
        VaultKeyAdminOperations(
            vaultOperations = vaultOperations,
            keyMaterialProvider = keyMaterialProvider,
            transitPath = kmsProperties.datasource.transitPath,
        )

    @Bean("providerAccessTokenSigner")
    fun accessTokenSigner(
        @Qualifier("providerKeyOperations") keyOperations: KeyOperations,
    ): AccessTokenSigner =
        AccessTokenSignerImpl(
            keyOperations = keyOperations,
            accessTokenPolicy = kmsProperties.accessTokenKeyPolicy,
        )

    @Bean("providerRefreshTokenKeyResolver")
    fun refreshTokenKeyResolver(
        @Qualifier("providerKeyMaterialProvider") keyMaterialProvider: KeyMaterialProvider,
    ): RefreshTokenKeyResolver =
        RefreshTokenKeyResolverImpl(
            keyMaterialProvider = keyMaterialProvider,
            refreshTokenPolicy = kmsProperties.refreshTokenKeyPolicy,
        )

}