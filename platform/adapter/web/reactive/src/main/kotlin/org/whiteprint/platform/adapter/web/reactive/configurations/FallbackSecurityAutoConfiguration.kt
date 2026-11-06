package org.whiteprint.platform.adapter.web.reactive.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.reactive.ReactiveManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import reactor.core.publisher.Mono

/**
 * security verifier 없이 웹 어댑터만 쓰는 앱(예: API 없는 워커)의 기본 보안 — servlet 쪽과 같은 규칙.
 *
 * 웹 어댑터가 spring-boot-starter-security 를 함께 가져오므로, verifier 가 필터 체인을 만들지 않으면
 * Boot 기본 보안이 켜진다 — 자동 생성 비밀번호의 Basic 인증이 health 외 전부를 막아서
 * /actuator/prometheus 가 401 이 되고, 기동 로그에 생성된 비밀번호가 찍힌다.
 *
 * 그 대신 verifier 와 같은 actuator 경로(health·prometheus)만 토큰 없이 열고 나머지는 전부 거부한다.
 * - verifier 가 있으면 그쪽 SecurityWebFilterChain 이 먼저 등록되므로 이 설정은 빠진다 (afterName + ConditionalOnMissingBean)
 * - Boot 기본 체인·actuator 체인·생성 사용자보다 먼저 등록돼야 그쪽이 빠진다 (before)
 * - API 를 여는 앱은 verifier 를 붙여야 한다 — 여기서는 어떤 API 경로도 열지 않는다
 */
@AutoConfiguration(
    afterName = ["org.whiteprint.platform.adapter.security.verifier.reactive.configuration.SecurityVerifierAutoConfiguration"],
    before = [
        ReactiveWebSecurityAutoConfiguration::class,
        ReactiveManagementWebSecurityAutoConfiguration::class,
        ReactiveUserDetailsServiceAutoConfiguration::class,
    ],
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(SecurityWebFilterChain::class)
@EnableWebFluxSecurity
class FallbackSecurityAutoConfiguration {

    @Bean
    fun fallbackSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .anonymous { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange { exchanges ->
                // verifier 와 같은 경로 — k8s probe·Prometheus 수집 (ACTUATOR_PATH_PATTERNS 주석 참고)
                exchanges.pathMatchers(HttpMethod.GET, *ACTUATOR_PATH_PATTERNS).permitAll()
                exchanges.anyExchange().denyAll()
            }
            // 거부는 403 — reactive 기본 진입점은 401(인증 요구)인데, 이 체인에는 로그인 수단이 없다 (servlet 쪽과 같은 응답)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.FORBIDDEN)) }
            .build()

    /**
     * 사용자 없는 ReactiveUserDetailsService — Boot 의 생성 사용자(ReactiveUserDetailsServiceAutoConfiguration)가 만들어지지 않게 한다.
     * 위 체인은 로그인 수단을 모두 껐으므로 실제로 호출되지 않는다.
     */
    @Bean
    fun fallbackReactiveUserDetailsService(): ReactiveUserDetailsService =
        ReactiveUserDetailsService { Mono.empty() }
}

// 토큰 없이 통과시키는 actuator 경로 — security verifier(reactive) 의 목록과 같게 유지
// - /actuator/health/** : /actuator/health 와 그 하위(liveness·readiness) 그룹
// - /actuator/prometheus : 클러스터 내부 Prometheus 수집용
private val ACTUATOR_PATH_PATTERNS = arrayOf("/actuator/health/**", "/actuator/prometheus")
