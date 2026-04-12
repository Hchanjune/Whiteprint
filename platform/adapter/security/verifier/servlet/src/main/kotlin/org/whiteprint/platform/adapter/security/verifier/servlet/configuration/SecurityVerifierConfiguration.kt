package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.whiteprint.platform.adapter.security.verifier.servlet.filter.StatelessSecurityFilter
import org.whiteprint.platform.adapter.security.verifier.servlet.security.AccessTokenVerificationKeyResolverImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.RevocationCheckerImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.SecurityEntryPointProvider
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.infra.security.jwt.verifier.JwtAccessTokenVerifier
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer

@Configuration
@EnableWebSecurity
class SecurityVerifierConfiguration(
    private val verificationProperties: SecurityVerifierConfigurationProperties,
    private val verificationCacheProperties: SecurityCacheConfigurationProperties
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    fun securityEntryPointProvider(): SecurityEntryPointProvider =
        SecurityEntryPointProvider(verificationProperties)

    @Bean
    fun revocationChecker(
        @Qualifier("securityCacheValueOperations") securityCache: ValueOperations
    ): RevocationChecker = RevocationCheckerImpl(
        cache = securityCache,
        keyStrategy = object : SecurityCacheKeyStrategy {},
        servicePrefix = verificationCacheProperties.cachePrefix
    )

    @Bean
    fun accessTokenKeyResolver(
        @Qualifier("verifierKeyMaterialService")  keyMaterialProvider: KeyMaterialProvider
    ): AccessTokenVerificationKeyResolver
        = AccessTokenVerificationKeyResolverImpl(
            keyMaterialProvider = keyMaterialProvider,
        )

    @Bean
    fun accessTokenVerifier(
        serializer: Serializer,
        accessTokenKeyResolver: AccessTokenVerificationKeyResolver,
        revocationChecker: RevocationChecker
    ): AccessTokenVerifier
        = JwtAccessTokenVerifier(
        serializer = serializer,
        keyResolver = accessTokenKeyResolver,
        revocationChecker = revocationChecker,
    )

    @Bean
    fun statelessSecurityFilter(
        serializer: Serializer,
        securityEntryPointProvider: SecurityEntryPointProvider,
        accessTokenVerifier: AccessTokenVerifier,
    ): StatelessSecurityFilter =
        StatelessSecurityFilter(
            serializer = serializer,
            securityEntryPointProvider = securityEntryPointProvider,
            accessTokenVerifier = accessTokenVerifier
        )

    @Bean
    fun statelessFilterChain(
        http: HttpSecurity,
        statelessSecurityFilter: StatelessSecurityFilter,
        securityEntryPointProvider: SecurityEntryPointProvider,
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight 허용
                auth.requestMatchers(HttpMethod.HEAD, "/**").denyAll()

                auth.requestMatchers(*securityEntryPointProvider.permitAllMatchers().toTypedArray()).permitAll()
                auth.requestMatchers(*securityEntryPointProvider.denyAllMatchers().toTypedArray()).denyAll()
                auth.requestMatchers(*securityEntryPointProvider.authenticateAllMatchers().toTypedArray()).authenticated()

                auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()

                auth.anyRequest().authenticated()
            }
            .addFilterBefore(statelessSecurityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .cors { it.configurationSource(corsConfigurationSource) }
            .headers { header ->
                header.frameOptions { it.sameOrigin() }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "traceparent")
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}