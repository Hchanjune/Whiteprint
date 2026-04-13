package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenVerificationKeyResolverImpl
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.infra.security.jwt.provider.JwtTokenProvider
import org.whiteprint.platform.infra.security.jwt.verifier.JwtRefreshTokenVerifier

@Configuration
class SecurityProviderConfiguration(
    private val properties: SecurityProviderConfigurationProperties,
    private val kmsProperties: SecurityProviderKmsConfigurationProperties
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }


    @Bean
    fun tokenProvider(
        serializer: Serializer,
        @Qualifier("providerAccessTokenSigner") accessTokenSigner: AccessTokenSigner,
        @Qualifier("providerRefreshTokenSigner") refreshTokenSigner: RefreshTokenSigner
    ): TokenProvider {
        return JwtTokenProvider(
            serializer = serializer,
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
            refreshTokenSigner = refreshTokenSigner
        )
    }

    @Bean("providerRefreshTokenVerificationKeyResolver")
    fun refreshTokenVerificationKeyResolver(
        @Qualifier("providerKeyMaterialProvider")  keyMaterialProvider: KeyMaterialProvider
    ): RefreshTokenVerificationKeyResolver =
        RefreshTokenVerificationKeyResolverImpl(
            keyMaterialProvider = keyMaterialProvider,
        )

    @Bean
    fun refreshTokenVerifier(
        serializer: Serializer,
        @Qualifier("providerRefreshTokenVerificationKeyResolver") refreshTokenVerificationKeyResolver: RefreshTokenVerificationKeyResolver,
        revocationChecker: RevocationChecker
    ): RefreshTokenVerifier =
            JwtRefreshTokenVerifier(
                serializer = serializer,
                keyResolver = refreshTokenVerificationKeyResolver,
                revocationChecker = revocationChecker
            )

}