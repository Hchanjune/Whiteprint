package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.whiteprint.platform.adapter.security.provider.servlet.key.AccessTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenSignerImpl
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.kms.service.KeyOperations
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner

/** 토큰 서명기. KMS 빈([KeyOperations]·[KeyAdminOperations])은 `adapter:security:kms` 가 등록한다. */
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

}
