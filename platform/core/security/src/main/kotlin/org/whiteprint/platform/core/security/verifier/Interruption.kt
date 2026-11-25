package org.whiteprint.platform.core.security.verifier

import java.nio.channels.ClosedByInterruptException

private const val MAX_CAUSE_DEPTH = 16

/**
 * 토큰 검증 도중 **스레드 인터럽트**로 끊긴 예외인지 판별한다 — 원인 사슬에 인터럽트가 있으면 그렇다.
 *
 * 요청 취소(리액티브 구독 취소 → boundedElastic 작업 dispose)나 종료 중에 블로킹 조회(폐기 확인 Redis 등)가
 * 끊긴 것이라 **토큰 검증 실패가 아니다.** `TOKEN_VERIFICATION_INTERNAL_ERROR` 로 분류하면 취소된 요청이
 * 검증 오류 로그·500 으로 섞여 나온다.
 *
 * 드라이버가 감싸서 던지므로 사슬을 따라간다
 * (예: `RedisSystemException` ← `RedisCommandInterruptedException` ← `InterruptedException`).
 * `InterruptedIOException` 은 일부러 보지 않는다 — `SocketTimeoutException` 이 그 하위라 타임아웃까지 삼킨다.
 */
fun Throwable.isInterruption(): Boolean =
    generateSequence(this) { current -> current.cause?.takeIf { it !== current } }
        .take(MAX_CAUSE_DEPTH)
        .any { it is InterruptedException || it is ClosedByInterruptException }
