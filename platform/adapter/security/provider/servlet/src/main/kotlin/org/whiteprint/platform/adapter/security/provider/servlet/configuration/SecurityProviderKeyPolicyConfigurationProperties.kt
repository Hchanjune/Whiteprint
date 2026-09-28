package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.whiteprint.platform.core.kms.model.KeyType

/**
 * 토큰 서명 키의 정책(별칭·로테이션·알고리즘). Vault 연결 자체는 `adapter.security.kms` 가 가진다.
 */
@ConfigurationProperties(prefix = "adapter.security.provider.key-policy")
data class SecurityProviderKeyPolicyConfigurationProperties(
    var accessToken: AccessTokenKeyPolicy = AccessTokenKeyPolicy(),
    var refreshToken: RefreshTokenKeyPolicy = RefreshTokenKeyPolicy(),
) {

    data class AccessTokenKeyPolicy(
        var keyAlias: String = "access-token-sig",

        var rotationIntervalSeconds: Long = 2592000,
        var overlapSeconds: Long = 86400,
        var algorithm: KeyType = KeyType.RSA_2048
    )

    data class RefreshTokenKeyPolicy(
        var keyAlias: String = "refresh-token-sig",

        var rotationIntervalSeconds: Long = 2592000,
        var overlapSeconds: Long = 86400,
        var algorithm: KeyType = KeyType.RSA_2048
    )

}
