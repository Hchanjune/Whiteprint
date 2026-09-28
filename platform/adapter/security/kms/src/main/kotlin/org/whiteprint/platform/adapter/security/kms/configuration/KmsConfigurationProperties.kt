package org.whiteprint.platform.adapter.security.kms.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * KMS(Vault) 연결 하나. JWT 서명·검증(provider/verifier)과 서비스 자체 암호화가 이 연결을 같이 쓴다.
 */
@ConfigurationProperties(prefix = "adapter.security.kms")
data class KmsConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var cache: CacheOptions = CacheOptions(),
) {

    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 8200,
        var password: String = "",
        var transitPath: String = "transit",
    )

    /** 공개키 자료 캐시. 검증할 때마다 Vault 를 부르지 않도록 둔다. */
    data class CacheOptions(
        var expiresAfterWriteMinutes: Long = 60,
        var maximumSize: Long = 1000,
    )
}
