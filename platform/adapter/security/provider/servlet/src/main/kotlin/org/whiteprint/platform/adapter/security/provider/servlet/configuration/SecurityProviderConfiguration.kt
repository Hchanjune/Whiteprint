package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.infra.security.jwt.provider.JwtTokenProvider
import org.whiteprint.platform.infra.security.jwt.verifier.JwtRefreshTokenVerifier

@Configuration
class SecurityProviderConfiguration(
    private val properties: SecurityProviderConfigurationProperties
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    @Bean
    fun tokenProvider(
        @Qualifier("providerAccessTokenSigner") accessTokenSigner: AccessTokenSigner,
        @Qualifier("providerRefreshTokenKeyResolver") refreshTokenKeyResolver: RefreshTokenKeyResolver
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
            accessTokenSigner = accessTokenSigner,
            refreshTokenKeyResolver = refreshTokenKeyResolver
        )
    }

    @Bean
    fun refreshTokenVerifier(
        @Qualifier("providerRefreshTokenKeyResolver") refreshTokenKeyResolver: RefreshTokenKeyResolver,
        revocationChecker: RevocationChecker
    ): RefreshTokenVerifier =
            JwtRefreshTokenVerifier(
                keyResolver = refreshTokenKeyResolver,
                revocationChecker = revocationChecker
            )

}