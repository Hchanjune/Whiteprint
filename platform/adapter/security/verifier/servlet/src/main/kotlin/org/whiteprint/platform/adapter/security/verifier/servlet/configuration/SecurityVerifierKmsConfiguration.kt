package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.core.kms.service.KeyAdminService
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialService
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.infra.kms.vault.VaultKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialService
import org.whiteprint.platform.infra.kms.vault.VaultKeyOperations
import org.whiteprint.platform.infra.kms.vault.VaultKeyAdminService

@Configuration
@EnableConfigurationProperties(SecurityVerifierKmsConfigurationProperties::class)
class SecurityVerifierKmsConfiguration(
    private val kmsProperties: SecurityVerifierKmsConfigurationProperties,
) {

    @Bean
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
        vaultOperations: VaultOperations,
    ): KeyMaterialService =
        VaultKeyMaterialService(vaultOperations)

    //Should be moved to Provider
    @Bean("verifierKeyAdminService")
    fun securityKeyAdminService(
        vaultOperations: VaultOperations,
    ): KeyAdminService =
        VaultKeyAdminService(vaultOperations)

    //Should be moved to Provider
    @Bean("verifierKeyOperations")
    fun securityKeyOperations(
        vaultOperations: VaultOperations,
    ): KeyOperations =
        VaultKeyOperations(vaultOperations)


}