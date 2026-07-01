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
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.whiteprint.platform.adapter.security.verifier.servlet.filter.StatelessSecurityFilter
import org.whiteprint.platform.adapter.security.verifier.servlet.security.AccessTokenVerificationKeyResolverImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.RevocationCheckerImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.PermittedEntryPointProvider
import org.whiteprint.platform.adapter.security.verifier.servlet.filter.SecurityAuthenticationEntryPoint
import org.whiteprint.platform.adapter.security.verifier.servlet.security.AccountTokenStatusManagerImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.TokenRevokerImpl
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.policy.RevocationPolicy
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.verifier.TokenRevoker
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import org.whiteprint.platform.core.security.verifier.AccountTokenStatusManager
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.infra.security.jwt.verifier.JwtAccessTokenVerifier
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import java.time.Duration

@Configuration
@EnableWebSecurity
class SecurityVerifierConfiguration(
    private val verificationProperties: SecurityVerifierConfigurationProperties,
    private val verificationCacheProperties: SecurityCacheConfigurationProperties,
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    fun securityEntryPointProvider(): PermittedEntryPointProvider =
        PermittedEntryPointProvider(verificationProperties)

    @Bean
    fun revocationPolicy(): RevocationPolicy =
        RevocationPolicy(
            accountRevocationDuration = Duration.ofMillis(verificationProperties.policy.revocation.accountRevocationMillis)
        )

    @Bean fun securityCacheKeyStrategy(): SecurityCacheKeyStrategy =
            object : SecurityCacheKeyStrategy {}

    @Bean
    fun accountTokenStatusManager(
        @Qualifier("securityCacheValueOperations") securityCache: ValueOperations,
        securityCacheKeyStrategy: SecurityCacheKeyStrategy,
    ): AccountTokenStatusManager =
        AccountTokenStatusManagerImpl(
            cache = securityCache,
            keyStrategy = securityCacheKeyStrategy,
            servicePrefix = verificationCacheProperties.cachePrefix,
            forceUpdateExpiration = Duration.ofMillis(verificationProperties.policy.revocation.accountRevocationMillis)
        )

    @Bean
    fun revoker(
        @Qualifier("securityCacheValueOperations") securityCache: ValueOperations,
        securityCacheKeyStrategy: SecurityCacheKeyStrategy,
    ): TokenRevoker = TokenRevokerImpl(
        cache = securityCache,
        keyStrategy = securityCacheKeyStrategy,
        servicePrefix = verificationCacheProperties.cachePrefix
    )

    @Bean
    fun revocationChecker(
        @Qualifier("securityCacheValueOperations")
        securityCache: ValueOperations,
        securityCacheKeyStrategy: SecurityCacheKeyStrategy,
    ): RevocationChecker = RevocationCheckerImpl(
        cache = securityCache,
        revocationKeyStrategy = securityCacheKeyStrategy,
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
        headerName = verificationProperties.policy.headerName,
        scheme = verificationProperties.policy.scheme,
        serializer = serializer,
        keyResolver = accessTokenKeyResolver,
        revocationChecker = revocationChecker,
    )

    @Bean
    fun securityContextRepository(): SecurityContextRepository =
        RequestAttributeSecurityContextRepository()

    @Bean
    fun statelessSecurityFilter(
        securityContextRepository: SecurityContextRepository,
        permittedEntryPointProvider: PermittedEntryPointProvider,
        accessTokenVerifier: AccessTokenVerifier,
    ): StatelessSecurityFilter =
        StatelessSecurityFilter(
            permittedEntryPointProvider = permittedEntryPointProvider,
            accessTokenVerifier = accessTokenVerifier,
            securityContextRepository = securityContextRepository
        )

    @Bean("securityAuthenticationEntryPoint")
    fun securityAuthenticationEntryPoint(
        serializer: Serializer,
    ): AuthenticationEntryPoint
        = SecurityAuthenticationEntryPoint(serializer)

    @Bean
    fun statelessFilterChain(
        http: HttpSecurity,
        permittedEntryPointProvider: PermittedEntryPointProvider,
        statelessSecurityFilter: StatelessSecurityFilter,
        corsConfigurationSource: CorsConfigurationSource,
        @Qualifier("securityAuthenticationEntryPoint") authenticationEntryPoint: AuthenticationEntryPoint,
        securityContextRepository: SecurityContextRepository,
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight
                //auth.requestMatchers(HttpMethod.HEAD, "/**").denyAll()
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                permittedEntryPointProvider.matchers().forEach {
                    auth.requestMatchers(it).permitAll()
                }
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(statelessSecurityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .cors { it.configurationSource(corsConfigurationSource) }
            .headers { header ->
                header.frameOptions { it.sameOrigin() }
            }
            .securityContext { it.securityContextRepository(securityContextRepository) }
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "traceparent")
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}