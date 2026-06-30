package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.infra.kms.vault.CaffeineKeyCache
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
        ).apply {
            this.scheme = "http"
        }
        val clientAuthentication = TokenAuthentication(kmsProperties.datasource.password)
        return VaultTemplate(endpoint, clientAuthentication)
    }

    @Bean("verifierVaultConnectionValidator")
    fun vaultConnectionValidator(
        @Qualifier("verifierVaultOperations") vaultOperations: VaultOperations
    ) = SmartInitializingSingleton {
        val logger = LoggerFactory.getLogger("VaultConnectionValidator-Verifier")
        try {
            val result = vaultOperations.read("sys/health")
            require(result != null) { "Vault health check failed: no response" }
            logger.info("Vault connection validation succeeded")
        } catch (e: Exception) {
            throw IllegalStateException("Vault connection validation failed", e)
        }
    }

    @Bean("verifierKmsCaffeineCache")
    fun securityKmsCache(): KeyCache =
        CaffeineKeyCache(
            expiresAfterWriteMinutes = kmsProperties.cache.expiresAfterWriteMinutes,
            maximumSize = kmsProperties.cache.maximumSize,
        )

    @Bean("verifierKeyMaterialService")
    fun securityKeyMaterialService(
        @Qualifier("verifierVaultOperations") vaultOperations: VaultOperations,
        @Qualifier("verifierKmsCaffeineCache") keyCache: KeyCache,
    ): KeyMaterialProvider =
        VaultKeyMaterialProvider(
            vaultOperations = vaultOperations,
            keyCache = keyCache,
            transitPath = kmsProperties.datasource.transitPath
        )
}
