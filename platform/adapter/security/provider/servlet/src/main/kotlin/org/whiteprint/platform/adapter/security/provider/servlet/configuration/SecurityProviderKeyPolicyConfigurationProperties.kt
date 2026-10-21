package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.whiteprint.platform.core.kms.model.KeyType

/**
 * 토큰 서명 키의 정책(별칭·로테이션·알고리즘). Vault 연결 자체는 `adapter.security.kms` 가 가진다.
 *
 * - 키는 기동 시 없으면 [algorithm] 으로 만들어진다. 이미 있는 키의 종류가 다르면 기동이 멈춘다.
 * - [AccessTokenKeyPolicy.rotationIntervalSeconds] 마다 새 버전으로 회전한다(0 이하면 회전 안 함). 옛 버전은 지우지 않는다.
 * - `overlapSeconds` 는 아직 읽는 곳이 없다 — 옛 버전 정리를 넣는다면 토큰 최대 수명보다 길어야 한다.
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
