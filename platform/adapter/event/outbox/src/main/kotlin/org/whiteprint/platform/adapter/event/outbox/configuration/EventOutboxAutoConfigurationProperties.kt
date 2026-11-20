package org.whiteprint.platform.adapter.event.outbox.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "adapter.event.outbox")
data class EventOutboxAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.JPA,
    /** 정리(purge) — 기본 꺼짐(하위호환). 서비스가 켠다. */
    var purge: Purge = Purge(),
) {
    /**
     * PUBLISHED 행 정리. 기준 시각은 occurred_at(발행 시각 컬럼이 없어 이벤트 발생 시각) — 발행은 보통 수 초 안이라 실질 차이 없다.
     * - [retention]: 이만큼 지난 행만 지운다(최소 1일).
     * - [cron]·[zone]: 실행 시각. 하루 한 번 새벽 권장 — 여러 인스턴스가 겹쳐 돌아도 삭제라 결과가 같다.
     * - [batchSize]: 한 번에 지우는 최대 행 수(트랜잭션·락·WAL 폭주 방지). 꽉 차면 이어서 반복한다.
     * - [maxBatchesPerRun]: 한 번 실행에서 반복하는 상한 — 남은 행은 다음 실행이 이어받는다.
     */
    data class Purge(
        var enabled: Boolean = false,
        var retention: Duration = Duration.ofDays(7),
        var cron: String = "0 0 4 * * *",
        var zone: String = "UTC",
        var batchSize: Int = 1000,
        var maxBatchesPerRun: Int = 100,
    )


    enum class InfrastructureImplementation {
        JPA,
        MONGO,
    }
}
