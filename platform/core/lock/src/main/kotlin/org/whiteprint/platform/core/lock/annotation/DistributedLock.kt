package org.whiteprint.platform.core.lock.annotation

import java.util.concurrent.TimeUnit

/**
 * 메서드를 분산 락 안에서 실행한다. 키는 `@DistributedLockKey` 가 붙은 파라미터(또는 파라미터 객체의 필드)로 만든다.
 *
 * ## 자동연장 (기본 켜짐)
 * 메서드가 도는 동안 [ttl] 의 1/3 간격으로 락을 연장한다. 그래서 [ttl] 은 **최악 처리 시간이 아니라 "보유자가 죽었을 때
 * 락이 풀리기까지 기다릴 시간"** 으로 잡는다 — 짧을수록 죽은 인스턴스의 락이 빨리 풀린다.
 *
 * 연장은 [maxHold] 까지만 한다. 스레드가 멈춰(무한 대기 등) 끝나지 않아도 락을 영원히 쥐지 않게 하는 상한이다.
 * 상한을 넘기면 연장을 멈추고 경고를 남기며, 락은 그 뒤 [ttl] 안에 만료된다.
 *
 * ⚠ 자동연장은 **살아서 진행 중인** 보유자의 락이 풀리는 것만 막는다. GC 멈춤·네트워크 단절로 연장이 끊겨 만료된 뒤
 * 옛 보유자가 깨어나 계속 쓰는 경우는 못 막는다 — 그건 펜싱 토큰 검사나 멱등성의 몫이다.
 *
 * @param prefix 키 앞에 붙는 이름공간. 권장 형식 `<서비스>:<대상>:v<n>`.
 * @param ttl 락 만료 시간. 자동연장이 켜져 있으면 연장 주기의 기준(1/3)이자, 보유자가 죽었을 때 풀리기까지의 시간.
 * @param wait 락을 기다리는 최대 시간. 못 잡으면 `LOCK_ACQUISITION_FAILED`(409).
 * @param timeUnit [ttl]·[wait]·[maxHold] 의 단위.
 * @param autoExtend 실행 중 자동연장 여부. 끄면 [ttl] 이 곧 최대 보유 시간이 된다(처리 시간보다 크게 잡아야 한다).
 * @param maxHold 자동연장 상한. 0 이하면 플랫폼 기본값(5분).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val wait: Long = 3000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    val autoExtend: Boolean = true,
    val maxHold: Long = 0L,
)
