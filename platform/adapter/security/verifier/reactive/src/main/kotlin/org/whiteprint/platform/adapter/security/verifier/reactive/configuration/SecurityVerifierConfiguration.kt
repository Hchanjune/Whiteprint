package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.whiteprint.platform.adapter.security.verifier.reactive.aspect.SecurityAuthorizationAspect
import org.whiteprint.platform.adapter.security.verifier.reactive.filter.SecurityServerAuthenticationEntryPoint
import org.whiteprint.platform.adapter.security.verifier.reactive.filter.StatelessWebSecurityFilter
import org.whiteprint.platform.adapter.security.verifier.reactive.security.AccessTokenVerificationKeyResolverImpl
import org.whiteprint.platform.adapter.security.verifier.reactive.security.AccountTokenStatusManagerImpl
import org.whiteprint.platform.adapter.security.verifier.reactive.security.PermittedPathProvider
import org.whiteprint.platform.adapter.security.verifier.reactive.security.RevocationCheckerImpl
import org.whiteprint.platform.adapter.security.verifier.reactive.security.TokenRevokerImpl
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.kms.service.KeyMaterialProvider
import org.whiteprint.platform.core.security.policy.RevocationPolicy
import org.whiteprint.platform.core.security.policy.SecurityCacheKeyStrategy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import org.whiteprint.platform.core.security.verifier.AccountTokenStatusManager
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.verifier.TokenRevoker
import org.whiteprint.platform.infra.security.jwt.verifier.JwtAccessTokenVerifier
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import java.time.Duration

@Configuration
@EnableWebFluxSecurity
class SecurityVerifierConfiguration(
    private val verificationProperties: SecurityVerifierConfigurationProperties,
    private val verificationCacheProperties: SecurityCacheConfigurationProperties,
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    fun permittedPathProvider(): PermittedPathProvider =
        PermittedPathProvider(verificationProperties)

    @Bean
    fun revocationPolicy(): RevocationPolicy =
        RevocationPolicy(
            accountRevocationDuration = Duration.ofMillis(verificationProperties.policy.revocation.accountRevocationMillis)
        )

    @Bean
    fun securityCacheKeyStrategy(): SecurityCacheKeyStrategy = object : SecurityCacheKeyStrategy {}

    @Bean
    fun accountTokenStatusManager(
        @Qualifier("securityCacheValueOperations") securityCache: ValueOperations,
        securityCacheKeyStrategy: SecurityCacheKeyStrategy,
    ): AccountTokenStatusManager = AccountTokenStatusManagerImpl(
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
        @Qualifier("securityCacheValueOperations") securityCache: ValueOperations,
        securityCacheKeyStrategy: SecurityCacheKeyStrategy,
    ): RevocationChecker = RevocationCheckerImpl(
        cache = securityCache,
        revocationKeyStrategy = securityCacheKeyStrategy,
        servicePrefix = verificationCacheProperties.cachePrefix
    )

    @Bean
    fun accessTokenKeyResolver(
        @Qualifier("verifierKeyMaterialService") keyMaterialProvider: KeyMaterialProvider
    ): AccessTokenVerificationKeyResolver = AccessTokenVerificationKeyResolverImpl(keyMaterialProvider)

    @Bean
    fun accessTokenVerifier(
        serializer: Serializer,
        accessTokenKeyResolver: AccessTokenVerificationKeyResolver,
        revocationChecker: RevocationChecker,
    ): AccessTokenVerifier = JwtAccessTokenVerifier(
        headerName = verificationProperties.policy.headerName,
        scheme = verificationProperties.policy.scheme,
        serializer = serializer,
        keyResolver = accessTokenKeyResolver,
        revocationChecker = revocationChecker,
    )

    @Bean
    fun statelessWebSecurityFilter(
        permittedPathProvider: PermittedPathProvider,
        accessTokenVerifier: AccessTokenVerifier,
    ): StatelessWebSecurityFilter = StatelessWebSecurityFilter(permittedPathProvider, accessTokenVerifier)

    @Bean
    fun securityServerAuthenticationEntryPoint(serializer: Serializer): SecurityServerAuthenticationEntryPoint =
        SecurityServerAuthenticationEntryPoint(serializer)

    @Bean
    fun securityAuthorizationAspect(): SecurityAuthorizationAspect = SecurityAuthorizationAspect()

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

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        permittedPathProvider: PermittedPathProvider,
        statelessWebSecurityFilter: StatelessWebSecurityFilter,
        corsConfigurationSource: CorsConfigurationSource,
        authenticationEntryPoint: SecurityServerAuthenticationEntryPoint,
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges.matchers(ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**")).permitAll()
                permittedPathProvider.entries().forEach { (method, pattern) ->
                    exchanges.matchers(ServerWebExchangeMatchers.pathMatchers(method, pattern)).permitAll()
                }
                exchanges.anyExchange().authenticated()
            }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .addFilterAt(statelessWebSecurityFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .cors { it.configurationSource(corsConfigurationSource) }
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
            .build()
    }
}
