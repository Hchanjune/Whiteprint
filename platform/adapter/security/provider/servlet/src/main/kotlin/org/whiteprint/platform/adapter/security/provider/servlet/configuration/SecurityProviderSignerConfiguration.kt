package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.whiteprint.platform.adapter.security.kms.provision.ManagedKey
import org.whiteprint.platform.adapter.security.kms.provision.ManagedKeyContributor
import org.whiteprint.platform.adapter.security.provider.servlet.key.AccessTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.rotation.SigningKeyRotator
import org.whiteprint.platform.core.kms.model.KeyType
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner
import java.time.Duration

/**
 * 토큰 서명기와 서명 키 관리(기동 시 생성·주기 회전). KMS 빈([KeyOperations]·[KeyAdminOperations])은 `adapter:security:kms` 가 등록한다.
 */
@Configuration
class SecurityProviderSignerConfiguration(
    private val keyPolicyProperties: SecurityProviderKeyPolicyConfigurationProperties,
) {

    @Bean("providerAccessTokenSigner")
    fun accessTokenSigner(
        keyOperations: KeyOperations,
        adminOperations: KeyAdminOperations,
    ): AccessTokenSigner =
        AccessTokenSignerImpl(
            keyOperations = keyOperations,
            adminOperations = adminOperations,
            accessTokenPolicy = keyPolicyProperties.accessToken,
        )

    @Bean("providerRefreshTokenSigner")
    fun refreshTokenSigner(
        keyOperations: KeyOperations,
        adminOperations: KeyAdminOperations,
    ): RefreshTokenSigner =
        RefreshTokenSignerImpl(
            keyOperations = keyOperations,
            adminOperations = adminOperations,
            refreshTokenPolicy = keyPolicyProperties.refreshToken,
        )


    /**
     * 서명 키 2개를 기동 시 없으면 만들게 KMS 에 알린다. 알고리즘은 RSA 만 받는다 —
     * 서명은 Vault `pkcs1v15`, 검증은 `SHA256withRSA` 로 고정이라 다른 종류면 키가 만들어져도 토큰이 안 나간다.
     */
    @Bean
    fun providerSigningKeyContributor() = ManagedKeyContributor {
        listOf(
            signingKey("access-token", keyPolicyProperties.accessToken.keyAlias, keyPolicyProperties.accessToken.algorithm),
            signingKey("refresh-token", keyPolicyProperties.refreshToken.keyAlias, keyPolicyProperties.refreshToken.algorithm),
        )
    }

    @Bean
    fun providerSigningKeyRotator(adminOperations: KeyAdminOperations) =
        SigningKeyRotator(
            adminOperations = adminOperations,
            policies = listOf(
                SigningKeyRotator.RotationPolicy(
                    keyAlias = keyPolicyProperties.accessToken.keyAlias,
                    interval = Duration.ofSeconds(keyPolicyProperties.accessToken.rotationIntervalSeconds),
                ),
                SigningKeyRotator.RotationPolicy(
                    keyAlias = keyPolicyProperties.refreshToken.keyAlias,
                    interval = Duration.ofSeconds(keyPolicyProperties.refreshToken.rotationIntervalSeconds),
                ),
            ),
        )

    private fun signingKey(name: String, alias: String, algorithm: KeyType): ManagedKey {
        require(algorithm in SUPPORTED_SIGNING_ALGORITHMS) {
            "adapter.security.provider.key-policy.$name.algorithm must be one of $SUPPORTED_SIGNING_ALGORITHMS (was $algorithm)"
        }
        return ManagedKey(alias = alias, type = algorithm)
    }

    private companion object {
        val SUPPORTED_SIGNING_ALGORITHMS = setOf(KeyType.RSA_2048, KeyType.RSA_4096)
    }
}
