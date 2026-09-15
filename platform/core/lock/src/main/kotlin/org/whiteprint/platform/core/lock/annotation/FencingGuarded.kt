package org.whiteprint.platform.core.lock.annotation

/**
 * 이 엔티티의 **갱신**을 분산 락 펜싱 토큰으로 검사한다(JPA `RootEntity` 대상, 옵트인).
 *
 * `@DistributedLock` 구간 안에서 이 엔티티를 저장하면:
 * - 현재 락의 토큰이 행에 기록된 `last_fencing_token` 보다 **작으면** 쓰기를 거절한다(`LOCK_FENCING_TOKEN_REJECTED`, 409)
 *   — 락을 잃은 옛 보유자가, 이미 새 보유자가 쓴 행을 덮어쓰는 경우다.
 * - 크거나 같으면 그 토큰을 행에 기록한다. 새로 만드는 행에도 기록한다.
 *
 * 락 구간 **밖**의 쓰기는 검사도 기록도 하지 않는다 — 락을 쓰지 않는 기존 경로가 그대로 동작한다.
 *
 * ## 무엇을 막고 무엇을 못 막나
 * - 막는 것: 옛 보유자가 **새 보유자의 쓰기 뒤에 행을 다시 읽고** 쓰는 경우. 다시 읽지 않고 쓰면 `@Version` 이 먼저 막는다.
 * - 못 막는 것: 벌크 UPDATE(JPQL·네이티브), 다른 스레드로 넘긴 쓰기(토큰이 스레드 로컬), 외부 시스템 호출(멱등 키의 몫).
 * - 한 엔티티는 **항상 같은 락 아래에서** 쓰는 것이 전제다. 토큰이 전역 카운터라 다른 키의 락끼리도 비교는 되지만,
 *   서로 다른 락이 같은 행을 지키면 정상적인 교차 쓰기도 순서에 따라 거절될 수 있다.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@java.lang.annotation.Inherited
annotation class FencingGuarded
