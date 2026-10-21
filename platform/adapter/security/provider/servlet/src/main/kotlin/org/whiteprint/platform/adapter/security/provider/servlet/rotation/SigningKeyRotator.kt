package org.whiteprint.platform.adapter.security.provider.servlet.rotation

import io.github.hchanjune.omk.core.annotations.ManagedSchedule
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import java.time.Clock
import java.time.Duration

/**
 * 토큰 서명 키를 주기마다 회전한다. 최신 버전이 만들어진 지 `rotation-interval-seconds` 가 지났으면 새 버전을 만든다.
 *
 * - 옛 버전은 지우지 않는다 — 토큰 헤더의 `ver` 로 검증하므로, 회전 전에 발급된 토큰은 만료될 때까지 그대로 통과한다.
 * - 판단 기준이 Vault 에 있는 버전의 생성 시각이라 재기동해도 주기가 초기화되지 않는다.
 * - 락이 없다. 인스턴스 여럿이 같은 순간에 돌면 버전이 하나 더 생길 뿐이다(둘 다 유효하고 검증도 된다).
 */
open class SigningKeyRotator(
    private val adminOperations: KeyAdminOperations,
    private val policies: List<RotationPolicy>,
    private val clock: Clock = Clock.systemUTC(),
) {

    /** [interval] 이 0 이하이면 그 키는 회전하지 않는다. */
    data class RotationPolicy(
        val keyAlias: String,
        val interval: Duration,
    )

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(initialDelay = 60_000, fixedDelay = CHECK_INTERVAL_MILLIS)
    @ManagedSchedule(quietWhenEmpty = true)
    open fun rotateDueKeys(): Int =
        policies.count { policy ->
            runCatching { rotateIfDue(policy) }
                .onFailure { logger.warn("Signing key rotation failed: alias={}", policy.keyAlias, it) }
                .getOrDefault(false)
        }

    private fun rotateIfDue(policy: RotationPolicy): Boolean {
        if (policy.interval <= Duration.ZERO) {
            return false
        }

        val latest = adminOperations.findMetadata(policy.keyAlias)
        if (latest == null) {
            logger.warn("Signing key not found, skipping rotation: alias={}", policy.keyAlias)
            return false
        }

        if (latest.createdAt.plus(policy.interval).isAfter(clock.instant())) {
            return false
        }

        val rotated = adminOperations.rotateKey(policy.keyAlias)
        logger.info(
            "Signing key rotated: alias={}, version {} -> {}",
            policy.keyAlias, latest.latestVersion, rotated.metadata.latestVersion,
        )
        return true
    }

    private companion object {
        /** 회전 주기(기본 30일)에 비해 충분히 촘촘하면 된다. */
        const val CHECK_INTERVAL_MILLIS = 3_600_000L
    }
}
