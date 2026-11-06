package org.whiteprint.platform.adapter.web.servlet.configurations

import jakarta.servlet.DispatcherType
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.SecurityFilterChain

/**
 * security verifier 없이 웹 어댑터만 쓰는 앱(예: API 없는 워커)의 기본 보안.
 *
 * 웹 어댑터가 spring-boot-starter-security 를 함께 가져오므로, verifier 가 필터 체인을 만들지 않으면
 * Boot 기본 보안이 켜진다 — 자동 생성 비밀번호의 Basic 인증이 health 외 전부를 막아서
 * /actuator/prometheus 가 401 이 되고, 기동 로그에 생성된 비밀번호가 찍힌다.
 *
 * 그 대신 verifier 와 같은 actuator 경로(health·prometheus)만 토큰 없이 열고 나머지는 전부 거부한다.
 * - verifier 가 있으면 그쪽 SecurityFilterChain 이 먼저 등록되므로 이 설정은 빠진다 (afterName + ConditionalOnMissingBean)
 * - Boot 기본 체인·actuator 체인·생성 사용자보다 먼저 등록돼야 그쪽이 빠진다 (before)
 * - API 를 여는 앱은 verifier 를 붙여야 한다 — 여기서는 어떤 API 경로도 열지 않는다
 */
@AutoConfiguration(
    afterName = ["org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityVerifierAutoConfiguration"],
    before = [
        ServletWebSecurityAutoConfiguration::class,
        ManagementWebSecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class,
    ],
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnMissingBean(SecurityFilterChain::class)
@EnableWebSecurity
class FallbackSecurityAutoConfiguration {

    @Bean
    fun fallbackSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .anonymous { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                // verifier 와 같은 경로 — k8s probe·Prometheus 수집 (ACTUATOR_PATH_PATTERNS 주석 참고)
                auth.requestMatchers(HttpMethod.GET, *ACTUATOR_PATH_PATTERNS).permitAll()
                auth.anyRequest().denyAll()
            }
            .build()

    /**
     * 사용자 없는 UserDetailsService — Boot 의 생성 사용자(UserDetailsServiceAutoConfiguration)가 만들어지지 않게 한다.
     * 위 체인은 로그인 수단을 모두 껐으므로 실제로 호출되지 않는다.
     */
    @Bean
    fun fallbackUserDetailsService(): UserDetailsService =
        UserDetailsService { username -> throw UsernameNotFoundException(username) }
}

// 토큰 없이 통과시키는 actuator 경로 — security verifier(servlet) 의 목록과 같게 유지
// - /actuator/health/** : /actuator/health 와 그 하위(liveness·readiness) 그룹
// - /actuator/prometheus : 클러스터 내부 Prometheus 수집용
private val ACTUATOR_PATH_PATTERNS = arrayOf("/actuator/health/**", "/actuator/prometheus")
