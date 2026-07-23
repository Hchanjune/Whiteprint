package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.subscriber")
data class EventSubscriberAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
    /**
     * stale PROCESSING 판정 임계 — "하트비트가 이 시간 이상 끊긴 PROCESSING = 처리 주체 사망"으로
     * 간주하고 RECEIVED 로 복귀시킨다. 처리 중인 이벤트는 30초마다 하트비트(last_attempted_at 갱신)를
     * 찍으므로, 이 값은 처리 시간과 무관하게 하트비트 간격의 3~4배(기본 120초)면 충분하다.
     * (구 claim-timeout-millis — 하트비트 도입으로 의미가 바뀌며 rename)
     */
    var staleTimeoutMillis: Long = 120_000L,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
