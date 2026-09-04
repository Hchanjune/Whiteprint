package org.whiteprint.platform.adapter.identifier

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.logging.DeferredLogFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.SystemEnvironmentPropertySource
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator

/**
 * 노드 번호 배선 검증.
 *
 * 단위 검증만으로는 부족하다 — `spring.factories` 등록이 실제로 먹는지, 그리고 **빈 생성보다 먼저**
 * 도는지가 이 어댑터의 존재 이유라서, 실제로 `SpringApplication` 을 띄워서 확인한다.
 */
class TsidNodeIdEnvironmentPostProcessorTest {

    @SpringBootApplication
    open class TestApplication

    /** [TsidGenerator] 는 프로세스 전역이라 테스트끼리 새지 않게 되돌린다. */
    @AfterEach
    fun resetNodeId() {
        TsidGenerator.configure(0)
    }

    private fun boot(vararg properties: String) =
        SpringApplicationBuilder(TestApplication::class.java)
            .web(WebApplicationType.NONE)
            .properties(*properties, "spring.main.banner-mode=off")
            .run()

    /** 스프링이 EnvironmentPostProcessor 의 예외를 감싸므로 원인 사슬 전체를 본다. */
    private fun causeChain(throwable: Throwable): List<String> =
        generateSequence(throwable, Throwable::cause).mapNotNull { it.message }.toList()

    /** 생성된 id 에서 노드 비트만 뽑는다 — `[timestamp 42][node 10][sequence 12]`. */
    private fun nodeIdOfGeneratedId(): Long = (TsidGenerator.generate() ushr 12) and 1023L

    private fun processorOn(environment: ConfigurableEnvironment) {
        val logFactory = DeferredLogFactory { supplier -> supplier.get() }
        TsidNodeIdEnvironmentPostProcessor(logFactory)
            .postProcessEnvironment(environment, SpringApplication())
    }

    @Test
    @DisplayName("service-id 만 주면 인스턴스 0 으로 합성된다 — 단일 인스턴스 기본값")
    fun composesWithDefaultInstanceId() {
        boot("platform.identifier.service-id=7").use {
            assertEquals(112L, nodeIdOfGeneratedId()) // 7 shl 4 or 0
        }
    }

    @Test
    @DisplayName("instance-id 를 주면 같은 서비스라도 노드 번호가 갈린다 — 스케일아웃 대응")
    fun composesWithInstanceId() {
        boot("platform.identifier.service-id=7", "platform.identifier.instance-id=3").use {
            assertEquals(115L, nodeIdOfGeneratedId()) // 7 shl 4 or 3
        }
    }

    @Test
    @DisplayName("같은 서비스의 두 인스턴스는 절대 같은 id 를 만들지 않는다")
    fun instancesOfSameServiceNeverCollide() {
        val first: Set<Long>
        val second: Set<Long>
        boot("platform.identifier.service-id=7", "platform.identifier.instance-id=0").use {
            first = (1..2_000).map { TsidGenerator.generate() }.toSet()
        }
        boot("platform.identifier.service-id=7", "platform.identifier.instance-id=1").use {
            second = (1..2_000).map { TsidGenerator.generate() }.toSet()
        }
        assertTrue(first.intersect(second).isEmpty(), "인스턴스가 다른데 겹친 id 가 있다")
    }

    @Test
    @DisplayName("service-id 미설정이면 기동에 실패한다 — 조용히 노드 0 으로 도는 것이 사고의 원인이었다")
    fun failsToStartWhenServiceIdUnset() {
        val failure = assertThrows(Throwable::class.java) { boot() }
        assertTrue(causeChain(failure).any { it.contains("platform.identifier.service-id") })
    }

    @Test
    @DisplayName("전환기에는 required=false 로 미설정을 허용한다")
    fun allowsUnsetWhenNotRequired() {
        boot("platform.identifier.required=false").use {
            assertEquals(0L, nodeIdOfGeneratedId())
        }
    }

    @Test
    @DisplayName("범위를 벗어난 번호는 기동에 실패한다 — 잘라내면 다른 서비스와 같은 번호가 될 수 있다")
    fun failsOnOutOfRangeIds() {
        val service = assertThrows(Throwable::class.java) { boot("platform.identifier.service-id=64") }
        assertTrue(causeChain(service).any { it.contains("0..63") })

        val instance = assertThrows(Throwable::class.java) {
            boot("platform.identifier.service-id=1", "platform.identifier.instance-id=16")
        }
        assertTrue(causeChain(instance).any { it.contains("0..15") })
    }

    @Test
    @DisplayName("숫자가 아니면 기동에 실패한다")
    fun failsOnNonNumericId() {
        val failure = assertThrows(Throwable::class.java) { boot("platform.identifier.service-id=abc") }
        assertTrue(causeChain(failure).any { it.contains("abc") })
    }

    @Test
    @DisplayName("환경변수 이름으로도 읽힌다 — 컨테이너에서 인스턴스 번호를 주입하는 경로")
    fun acceptsEnvironmentVariableStyle() {
        // 느슨한 바인딩은 **환경변수 소스**에만 적용된다(시스템 프로퍼티가 아니다).
        // 그래서 환경변수와 같은 프로퍼티 소스를 직접 얹어 그 경로를 태운다.
        val environment = StandardEnvironment()
        environment.propertySources.addFirst(
            SystemEnvironmentPropertySource(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                mapOf<String, Any>(
                    "PLATFORM_IDENTIFIER_SERVICE_ID" to "9",
                    "PLATFORM_IDENTIFIER_INSTANCE_ID" to "2",
                ),
            )
        )

        processorOn(environment)

        assertEquals(146L, nodeIdOfGeneratedId()) // 9 shl 4 or 2
    }
}
