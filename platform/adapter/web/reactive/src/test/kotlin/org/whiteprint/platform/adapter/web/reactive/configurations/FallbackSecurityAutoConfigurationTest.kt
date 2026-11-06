package org.whiteprint.platform.adapter.web.reactive.configurations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.reactive.ReactiveManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterChainProxy
import reactor.core.publisher.Mono

/**
 * servlet 쪽 FallbackSecurityAutoConfigurationTest 와 같은 확인 — reactive 스택.
 */
class FallbackSecurityAutoConfigurationTest {

    private val runner = ReactiveWebApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                FallbackSecurityAutoConfiguration::class.java,
                ReactiveWebSecurityAutoConfiguration::class.java,
                ReactiveManagementWebSecurityAutoConfiguration::class.java,
                ReactiveUserDetailsServiceAutoConfiguration::class.java,
            )
        )

    @Test
    @DisplayName("verifier 가 없으면 fallback 체인이 쓰이고 Boot 생성 사용자는 만들어지지 않는다")
    fun fallbackReplacesBootDefaults() {
        runner.run { context ->
            assertTrue(context.containsBean("fallbackSecurityWebFilterChain"))
            assertEquals(1, context.getBeansOfType(SecurityWebFilterChain::class.java).size)
            // MapReactiveUserDetailsService = Boot 가 생성 비밀번호로 만드는 사용자 저장소
            assertFalse(context.getBean(ReactiveUserDetailsService::class.java) is MapReactiveUserDetailsService)
        }
    }

    @Test
    @DisplayName("actuator health·prometheus 는 인증 없이 통과하고 나머지는 403")
    fun actuatorOpenEverythingElseDenied() {
        runner.run { context ->
            assertEquals(200, status(context, HttpMethod.GET, "/actuator/prometheus"))
            assertEquals(200, status(context, HttpMethod.GET, "/actuator/health"))
            assertEquals(200, status(context, HttpMethod.GET, "/actuator/health/readiness"))
            assertEquals(403, status(context, HttpMethod.GET, "/actuator/env"))
            assertEquals(403, status(context, HttpMethod.GET, "/api/v1/anything"))
            assertEquals(403, status(context, HttpMethod.POST, "/actuator/prometheus"))
        }
    }

    @Test
    @DisplayName("다른 SecurityWebFilterChain(verifier) 이 있으면 fallback 은 빠진다")
    fun backsOffWhenVerifierChainExists() {
        runner.withUserConfiguration(VerifierLikeChain::class.java).run { context ->
            assertFalse(context.containsBean("fallbackSecurityWebFilterChain"))
            assertFalse(context.containsBean("fallbackReactiveUserDetailsService"))
            assertTrue(context.containsBean("verifierChain"))
        }
    }

    private fun status(context: ApplicationContext, method: HttpMethod, path: String): Int {
        val proxy = context.getBean(WebFilterChainProxy::class.java)
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.method(method, path).build())
        // 필터를 통과하면 체인 끝(여기서는 200 을 쓰는 핸들러)에 닿는다
        proxy.filter(exchange) { ex ->
            ex.response.statusCode = org.springframework.http.HttpStatus.OK
            Mono.empty()
        }.block()
        return exchange.response.statusCode!!.value()
    }

    @Configuration
    class VerifierLikeChain {
        @Bean
        fun verifierChain(http: ServerHttpSecurity): SecurityWebFilterChain =
            http.authorizeExchange { it.anyExchange().permitAll() }.build()
    }
}
