package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.whiteprint.platform.adapter.security.provider.servlet.resolver.AccessTokenSigningKeyResolverImpl
import org.whiteprint.platform.adapter.security.provider.servlet.resolver.RefreshTokenKeyResolverImpl
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigningKeyResolver
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.platform.infra.security.jwt.provider.JwtTokenProvider

@Configuration
class SecurityProviderConfiguration(
    private val properties: SecurityProviderConfigurationProperties
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun accessTokenSigningKeyResolver(): AccessTokenSigningKeyResolver =
        AccessTokenSigningKeyResolverImpl()

    @Bean
    fun refreshTokenKeyResolver(): RefreshTokenKeyResolver =
        RefreshTokenKeyResolverImpl()

    @Bean
    fun tokenProvider(
        accessTokenSigningKeyResolver: AccessTokenSigningKeyResolver,
        refreshTokenKeyResolver: RefreshTokenKeyResolver
    ): TokenProvider {
        return JwtTokenProvider(
            policy = TokenPolicy(
                accessTokenPolicy = TokenPolicy.AccessTokenPolicy(
                    issuer = properties.accessTokenPolicy.issuer,
                    expirationSeconds = properties.accessTokenPolicy.expirationSeconds
                ),
                refreshTokenPolicy = TokenPolicy.RefreshTokenPolicy(
                    issuer = properties.refreshTokenPolicy.issuer,
                    expirationSeconds = properties.refreshTokenPolicy.expirationSeconds
                )
            ),
            accessTokenSigningKeyResolver = accessTokenSigningKeyResolver,
            refreshTokenKeyResolver = refreshTokenKeyResolver
        )
    }

}