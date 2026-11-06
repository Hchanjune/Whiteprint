package org.whiteprint.platform.adapter.web.servlet.configurations

import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.DefaultPropertiesPropertySource
import org.springframework.core.env.ConfigurableEnvironment

/**
 * actuator 노출 기본값을 심는다 — 서비스마다 yaml 에 같은 설정을 반복하지 않게 한다.
 *
 * - 웹 노출: health(k8s probe), prometheus(클러스터 내부 수집). Boot 기본값은 health 뿐이다.
 * - probe 그룹(/actuator/health/liveness·readiness): Boot 는 k8s 를 감지했을 때만 켠다. 감지와 무관하게 늘 켜서
 *   로컬에서도 같은 경로로 확인할 수 있게 한다.
 *
 * **기본값(defaultProperties)으로 넣는다** — 우선순위가 가장 낮아서 서비스 yaml·환경변수에 적은 값이 항상 이긴다.
 * 토큰 없이 통과시키는 것은 security verifier 쪽 설정이고(verifier 가 없으면 FallbackSecurityAutoConfiguration), 외부 노출은 입구(gateway)에서 막는다.
 */
class ActuatorDefaultsEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        DefaultPropertiesPropertySource.addOrMerge(DEFAULTS, environment.propertySources)
    }

    private companion object {
        val DEFAULTS: Map<String, Any> = mapOf(
            "management.endpoints.web.exposure.include" to "health,prometheus",
            "management.endpoint.health.probes.enabled" to true,
        )
    }
}
