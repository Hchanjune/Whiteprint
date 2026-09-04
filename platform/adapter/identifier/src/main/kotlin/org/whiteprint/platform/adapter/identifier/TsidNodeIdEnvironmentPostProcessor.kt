package org.whiteprint.platform.adapter.identifier

import org.apache.commons.logging.Log
import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.logging.DeferredLogFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator

/**
 * 이 인스턴스의 노드 번호를 [TsidGenerator] 에 심는다.
 *
 * **[EnvironmentPostProcessor] 인 이유는 시점 때문이다.** 빈 생성보다 먼저 돌아야 한다 —
 * 자동설정이나 `@PostConstruct` 로 하면 그보다 앞서 id 를 만드는 빈이 하나라도 있을 때
 * 그 빈만 조용히 노드 0 으로 발급받는다. 여기서 하면 그 창이 없다.
 *
 * ```yaml
 * platform:
 *   identifier:
 *     service-id: 7      # 필수. 서비스마다 고유. 배정표는 레포 문서로 관리한다.
 *     instance-id: 0     # 선택. 기본 0 = 단일 인스턴스. 스케일아웃하면 인스턴스마다 달라야 한다.
 *     required: true     # 기본값. false 면 service-id 미설정을 허용한다(전환기용).
 * ```
 *
 * 값은 스프링 `Environment` 에서 읽으므로 yaml·환경변수·시스템 프로퍼티가 모두 통한다.
 * 컨테이너에서는 `PLATFORM_IDENTIFIER_INSTANCE_ID` 로 주입하면 되고 코드는 그대로다.
 *
 * ## 노드 번호가 두 조각인 이유
 *
 * ```
 * node(10) = service(6, 정적)  |  instance(4, 배포 시 주입)
 *            64개 서비스           서비스당 16 인스턴스
 * ```
 *
 * 스케일되는 것은 인스턴스지 서비스가 아니다 — 서비스마다 이미지도 설정도 다르므로 번호를 yaml 에 박아도 되고,
 * 같은 이미지로 여러 개 뜨는 인스턴스만 배포 시점에 구분되면 된다.
 *
 * **레이아웃을 지금 고정해 두는 것이 요점이다.** 나중에 인스턴스 번호를 자동 배정(임차)으로 바꿔도
 * 이미 발급된 id 의 형식이 흔들리지 않는다.
 *
 * ⚠ **스케일아웃하면서 [INSTANCE_ID_PROPERTY] 를 주지 않으면 모든 인스턴스가 0 이 되어 id 가 겹친다.**
 * 밀리초당 시퀀스는 프로세스 안에서만 유일하므로, 같은 노드 번호를 쓰는 두 인스턴스는 같은 밀리초에
 * **같은 id 를 만든다**(확률이 아니라 확정이다). 그래서 부팅 로그에 해석된 번호를 남긴다.
 */
class TsidNodeIdEnvironmentPostProcessor(
    logFactory: DeferredLogFactory,
) : EnvironmentPostProcessor {

    private val log: Log = logFactory.getLog(TsidNodeIdEnvironmentPostProcessor::class.java)

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val serviceId = resolveServiceId(environment) ?: return
        val instanceId = resolveInstanceId(environment)

        val nodeId = (serviceId shl INSTANCE_ID_BITS) or instanceId
        TsidGenerator.configure(nodeId)

        if (instanceId == 0L) {
            log.info(
                "식별자 노드 번호 = $nodeId (service=$serviceId, instance=0). " +
                    "$INSTANCE_ID_PROPERTY 가 없어 단일 인스턴스로 본다 — " +
                    "여러 인스턴스로 띄우려면 인스턴스마다 다른 값을 주입해야 id 가 겹치지 않는다."
            )
        } else {
            log.info("식별자 노드 번호 = $nodeId (service=$serviceId, instance=$instanceId)")
        }
    }

    /** 미설정이고 [REQUIRED_PROPERTY] 가 참이면 기동을 막는다. 거짓이면 설정을 건너뛴다(노드 0 유지). */
    private fun resolveServiceId(environment: ConfigurableEnvironment): Long? {
        val raw = environment.getProperty(SERVICE_ID_PROPERTY)
        if (raw.isNullOrBlank()) {
            check(!environment.getProperty(REQUIRED_PROPERTY, Boolean::class.java, true)) {
                "$SERVICE_ID_PROPERTY 가 설정되지 않았다. 서비스마다 고유한 번호를 배정해야 " +
                    "서비스 간 식별자 충돌이 없다(0..$MAX_SERVICE_ID). " +
                    "전환기라 미설정을 허용하려면 $REQUIRED_PROPERTY=false 로 둘 것."
            }
            log.warn("$SERVICE_ID_PROPERTY 가 없어 노드 0 으로 돈다 — 서비스 간 식별자가 겹칠 수 있다.")
            return null
        }
        return parse(raw, SERVICE_ID_PROPERTY, MAX_SERVICE_ID)
    }

    private fun resolveInstanceId(environment: ConfigurableEnvironment): Long {
        val raw = environment.getProperty(INSTANCE_ID_PROPERTY)
        if (raw.isNullOrBlank()) return 0
        return parse(raw, INSTANCE_ID_PROPERTY, MAX_INSTANCE_ID)
    }

    /**
     * 범위를 벗어나면 기동을 막는다 — 조용히 잘라내면 **다른 서비스/인스턴스와 같은 번호**가 될 수 있다.
     */
    private fun parse(raw: String, property: String, max: Long): Long {
        val value = raw.trim().toLongOrNull() ?: error("$property 는 숫자여야 한다: '$raw'")
        require(value in 0..max) { "$property 는 0..$max 범위여야 한다: $value" }
        return value
    }

    private companion object {
        const val SERVICE_ID_PROPERTY = "platform.identifier.service-id"
        const val INSTANCE_ID_PROPERTY = "platform.identifier.instance-id"
        const val REQUIRED_PROPERTY = "platform.identifier.required"

        /**
         * 10비트를 서비스 6 / 인스턴스 4 로 나눈다. 서비스가 12개라 64 는 넉넉하고,
         * 한 서비스를 16 인스턴스 넘게 띄울 일이 생기면 이 분할을 다시 봐야 한다
         * (바꿔도 유일성과 정렬은 유지되지만 같은 서비스의 노드 번호가 한 번 달라진다).
         */
        const val SERVICE_ID_BITS = 6
        const val INSTANCE_ID_BITS = 4
        const val MAX_SERVICE_ID = (1L shl SERVICE_ID_BITS) - 1
        const val MAX_INSTANCE_ID = (1L shl INSTANCE_ID_BITS) - 1
    }
}
