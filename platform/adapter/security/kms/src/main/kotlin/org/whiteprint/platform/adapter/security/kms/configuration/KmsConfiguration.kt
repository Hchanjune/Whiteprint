package org.whiteprint.platform.adapter.security.kms.configuration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
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
import org.whiteprint.platform.infra.kms.vault.CaffeineKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyAdminOperations
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialProvider
import org.whiteprint.platform.infra.kms.vault.VaultKeyOperations

/**
 * Vault 연결과 KMS 빈을 **한 번만** 등록한다. provider·verifier 는 여기 빈을 타입으로 받아 쓴다.
 *
 * 모든 빈이 `@ConditionalOnMissingBean` 이라 서비스가 같은 타입을 직접 등록하면 그쪽이 이긴다.
 * 연결은 blocking [VaultTemplate] 하나라 servlet·reactive 구분이 없다.
 */
@Configuration
class KmsConfiguration(
    private val properties: KmsConfigurationProperties,
) {

    @Bean
    @ConditionalOnMissingBean
    fun kmsVaultOperations(): VaultOperations {
        val endpoint = VaultEndpoint.create(
            properties.datasource.host,
            properties.datasource.port,
        ).apply {
            this.scheme = "http"
        }
        return VaultTemplate(endpoint, TokenAuthentication(properties.datasource.password))
    }

    @Bean
    fun kmsVaultConnectionValidator(vaultOperations: VaultOperations) =
        SmartInitializingSingleton {
            val logger = LoggerFactory.getLogger("VaultConnectionValidator-Kms")
            try {
                val result = vaultOperations.read("sys/health")
                require(result != null) { "Vault health check failed: no response" }
                logger.info("Vault connection validation succeeded")
            } catch (e: Exception) {
                throw IllegalStateException("Vault connection validation failed", e)
            }
        }

    @Bean
    @ConditionalOnMissingBean
    fun kmsKeyOperations(vaultOperations: VaultOperations): KeyOperations =
        VaultKeyOperations(
            vaultOperations = vaultOperations,
            transitPath = properties.datasource.transitPath,
        )

    @Bean
    @ConditionalOnMissingBean
    fun kmsKeyCache(): KeyCache =
        CaffeineKeyCache(
            expiresAfterWriteMinutes = properties.cache.expiresAfterWriteMinutes,
            maximumSize = properties.cache.maximumSize,
        )

    @Bean
    @ConditionalOnMissingBean
    fun kmsKeyMaterialProvider(
        vaultOperations: VaultOperations,
        keyCache: KeyCache,
    ): KeyMaterialProvider =
        VaultKeyMaterialProvider(
            vaultOperations = vaultOperations,
            keyCache = keyCache,
            transitPath = properties.datasource.transitPath,
        )

    @Bean
    @ConditionalOnMissingBean
    fun kmsKeyAdminOperations(
        vaultOperations: VaultOperations,
        keyMaterialProvider: KeyMaterialProvider,
    ): KeyAdminOperations =
        VaultKeyAdminOperations(
            vaultOperations = vaultOperations,
            keyMaterialProvider = keyMaterialProvider,
            transitPath = properties.datasource.transitPath,
        )
}
