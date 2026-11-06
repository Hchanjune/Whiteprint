package org.whiteprint.platform.adapter.web.servlet.configurations

import jakarta.servlet.Filter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.context.WebApplicationContext

/**
 * verifier 없는 앱에서 Boot 기본 보안(생성 비밀번호 + Basic) 대신 fallback 체인이 쓰이는지,
 * 그리고 verifier(= 다른 SecurityFilterChain)가 있으면 fallback 이 빠지는지 확인한다.
 */
class FallbackSecurityAutoConfigurationTest {

    // Boot 의 보안 자동설정과 함께 올려서 "누가 이기는가" 를 실제 순서대로 본다
    private val runner = WebApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                FallbackSecurityAutoConfiguration::class.java,
                SecurityAutoConfiguration::class.java,
                ServletWebSecurityAutoConfiguration::class.java,
                ManagementWebSecurityAutoConfiguration::class.java,
                UserDetailsServiceAutoConfiguration::class.java,
            )
        )

    @Test
    @DisplayName("verifier 가 없으면 fallback 체인이 쓰이고 Boot 생성 사용자는 만들어지지 않는다")
    fun fallbackReplacesBootDefaults() {
        runner.run { context ->
            assertTrue(context.containsBean("fallbackSecurityFilterChain"))
            assertEquals(1, context.getBeansOfType(SecurityFilterChain::class.java).size)
            // InMemoryUserDetailsManager = Boot 가 생성 비밀번호로 만드는 사용자 저장소
            assertFalse(context.getBean(UserDetailsService::class.java) is InMemoryUserDetailsManager)
        }
    }

    @Test
    @DisplayName("actuator health·prometheus 는 인증 없이 통과하고 나머지는 403")
    fun actuatorOpenEverythingElseDenied() {
        runner.run { context ->
            assertEquals(200, status(context, "GET", "/actuator/prometheus"))
            assertEquals(200, status(context, "GET", "/actuator/health"))
            assertEquals(200, status(context, "GET", "/actuator/health/readiness"))
            // Basic 인증 요구(401)가 아니라 거부(403) — 로그인 수단이 없다
            assertEquals(403, status(context, "GET", "/actuator/env"))
            assertEquals(403, status(context, "GET", "/api/v1/anything"))
            assertEquals(403, status(context, "POST", "/actuator/prometheus"))
        }
    }

    @Test
    @DisplayName("다른 SecurityFilterChain(verifier) 이 있으면 fallback 은 빠진다")
    fun backsOffWhenVerifierChainExists() {
        runner.withUserConfiguration(VerifierLikeChain::class.java).run { context ->
            assertFalse(context.containsBean("fallbackSecurityFilterChain"))
            assertFalse(context.containsBean("fallbackUserDetailsService"))
            assertTrue(context.containsBean("verifierChain"))
        }
    }

    private fun status(context: WebApplicationContext, method: String, path: String): Int {
        val proxy = context.getBean("springSecurityFilterChain", Filter::class.java)
        val request = MockHttpServletRequest(context.servletContext, method, path)
        val response = MockHttpServletResponse()
        // 필터를 통과하면 MockFilterChain 끝에 닿아 기본 200 으로 남는다
        proxy.doFilter(request, response, MockFilterChain())
        return response.status
    }

    @Configuration
    class VerifierLikeChain {
        @Bean
        fun verifierChain(http: HttpSecurity): SecurityFilterChain =
            http.authorizeHttpRequests { it.anyRequest().permitAll() }.build()
    }
}
