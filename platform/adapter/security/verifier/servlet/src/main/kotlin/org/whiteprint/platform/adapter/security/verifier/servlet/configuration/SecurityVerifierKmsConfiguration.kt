package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.infra.kms.vault.VaultKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialProvider

@Configuration
class SecurityVerifierKmsConfiguration(
    private val kmsProperties: SecurityVerifierKmsConfigurationProperties,
) {

    @Bean("verifierVaultOperations")
    fun vaultOperations(): VaultOperations {
        val endpoint = VaultEndpoint.create(
            kmsProperties.datasource.host,
            kmsProperties.datasource.port,
        )

        val clientAuthentication = TokenAuthentication(kmsProperties.datasource.password)

        return VaultTemplate(endpoint, clientAuthentication)
    }

    @Bean("verifierKmsCache")
    fun securityKmsCache(): KeyCache =
        VaultKeyCache()


    @Bean("verifierKeyMaterialService")
    fun securityKeyMaterialService(
        @Qualifier("verifierVaultOperations") vaultOperations: VaultOperations,
    ): KeyMaterialProvider =
        VaultKeyMaterialProvider(vaultOperations)




}