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

// 토큰 없이 통과시키는 actuator 경로
// - /actuator/health/** : /actuator/health 와 그 하위(liveness·readiness) 그룹 — `/**` 는 0개 세그먼트도 맞으므로 /actuator/health 자체도 포함
// - /actuator/prometheus : 클러스터 내부 Prometheus 수집용
private val ACTUATOR_PATH_PATTERNS = arrayOf("/actuator/health/**", "/actuator/prometheus")

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
        keyMaterialProvider: KeyMaterialProvider
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
            // stateless JWT 체인에는 익명 주체가 쓸 데가 없다. 켜두면 비인증 요청에서
            // authentication 이 null 이 아니라 AnonymousAuthenticationToken 이 되고,
            // 그 name 이 "anonymousUser" 라는 비어 있지 않은 문자열이라
            // 인증 여부를 name 으로 판정하는 코드가 조용히 틀린다.
            // (SecurityContextSupport 는 이 설정과 무관하게 익명을 걸러내지만, 근원을 막아둔다.)
            .anonymous { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight
                //auth.requestMatchers(HttpMethod.HEAD, "/**").denyAll()
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                // k8s probe·Prometheus 는 토큰 없이 호출한다. 막으면 401 로 probe 가 실패해 Pod 가 재시작을 반복한다.
                // health 는 기본 show-details=never 라 상태(UP/DOWN)만 나간다. 다른 actuator 엔드포인트는 열지 않는다.
                // 외부 노출은 입구(gateway)에서 막는다 — 서비스는 ClusterIP 라 클러스터 밖에서 직접 닿지 않는다.
                auth.requestMatchers(HttpMethod.GET, *ACTUATOR_PATH_PATTERNS).permitAll()
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