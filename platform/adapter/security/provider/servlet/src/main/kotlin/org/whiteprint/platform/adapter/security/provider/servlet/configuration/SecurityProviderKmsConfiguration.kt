package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.kms.service.KeyOperations
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
        )

        val clientAuthentication = TokenAuthentication(kmsProperties.datasource.password)

        return VaultTemplate(endpoint, clientAuthentication)
    }

    @Bean("providerKeyOperations")
    fun keyOperations(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
    ): KeyOperations =
        VaultKeyOperations(vaultOperations)

    @Bean("providerKmsCache")
    fun securityKmsCache(): KeyCache =
        CaffeineKeyCache()


    @Bean("providerKeyMaterialService")
    fun securityKeyMaterialService(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
    ): KeyMaterialProvider =
        VaultKeyMaterialProvider(vaultOperations)

    @Bean("providerKeyAdminService")
    fun securityKeyAdminService(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
    ): KeyAdminOperations =
        VaultKeyAdminOperations(vaultOperations)

    @Bean("providerKeyOperations")
    fun securityKeyOperations(
        @Qualifier("providerVaultOperations") vaultOperations: VaultOperations,
    ): KeyOperations =
        VaultKeyOperations(vaultOperations)
}