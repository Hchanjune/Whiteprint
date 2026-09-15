package org.whiteprint.platform.adapter.cache.common.aspect

import org.springframework.core.Ordered

/**
 * 캐시 애스펙트들의 실행 순서. **작을수록 바깥**이다.
 *
 * ## 왜 명시해야 하는가
 * `@Order` 가 없으면 `LOWEST_PRECEDENCE` 라 **가장 안쪽**에 놓인다. 그러면 OMK 의
 * `ManagedRepositoryAspect`(`HIGHEST + 15`, 클래스 단위 `@within`)가 바깥에 오고,
 * **캐시 히트여도 `[DB ]` 스팬이 생긴다** — DB 를 치지도 않았는데 DB 호출로 집계되고,
 * 그 스팬이 재는 시간은 Postgres 가 아니라 Redis 왕복이다.
 *
 * ## 왜 하필 이 구간인가 (OMK 기준 `HIGHEST + 10` ~ `+ 15` 사이)
 * ```
 * HIGHEST + 5   ManagedController / ManagedEventHandler / ManagedSchedule
 * HIGHEST + 10  ManagedOperation          ← 캐시보다 바깥이어야 한다
 * HIGHEST + 11  RateLimited
 * HIGHEST + 12  DistributedLock           ← adapter:lock 의 DistributedLockAspectOrder (이 모듈에 의존하지 않아 값만 맞춘다)
 * HIGHEST + 13  Deduplicated
 * HIGHEST + 14  Idempotent / Cached / CacheEvict
 * HIGHEST + 15  ManagedRepository / ManagedCacheRepository  ← 캐시보다 안쪽이어야 한다
 * LOWEST        @Transactional (기본값)     ← 락·캐시 전부 트랜잭션 바깥
 * ```
 * - **`ManagedOperation` 보다 안쪽**: 레이트리밋 거절이나 중복 스킵도 "그 유스케이스가 일어난 일"로
 *   기록돼야 한다. 바깥에 두면 거절된 요청이 트레이스에서 통째로 사라진다.
 * - **`ManagedRepository` 보다 바깥**: 캐시 히트면 저장소에 가지 않았으므로 `[DB ]` 스팬이 없어야 한다.
 *
 * ## 캐시 애스펙트끼리의 순서
 * 한 메서드에 여럿이 붙을 수 있어서 서로의 순서도 정해둔다 — 같은 값이면 Spring 이 정하는 순서가
 * 사실상 임의라 재현되지 않는다.
 *
 * ## 분산 락이 끼는 자리 (+12)
 * - **레이트리밋보다 안쪽**: 거절될 요청이 락을 기다리며 슬롯을 차지하지 않게.
 * - **중복 스킵·멱등보다 바깥**: 멱등은 read-through 라 동시에 온 같은 요청 둘이 모두 캐시 미스를 보고 둘 다 실행한다.
 *   락 안쪽에 두면 두 번째가 기다렸다가 첫 번째가 남긴 결과를 받는다.
 *
 * 5개를 11..14 네 칸에 넣어야 해서 [IDEMPOTENT] 가 [CACHED]·[CACHE_EVICT] 와 같은 값을 쓴다.
 * 캐시 두 개가 15 보다 작아야 하는 제약(저장소 메서드에도 붙는다) 때문에 위로 밀 수 없다.
 * 멱등과 읽기 캐시는 같은 메서드에 붙지 않고(쓰기 vs 읽기), 멱등 + 무효화는 어느 순서여도 안전하다 —
 * 무효화가 안쪽이면 재응답 때 건너뛰고(첫 실행에서 이미 지웠다), 바깥이면 한 번 더 지울 뿐이다.
 */
object CacheAspectOrder {

    /** 가장 먼저 거절한다 — 뒤의 작업을 아예 시작하지 않기 위해서. */
    const val RATE_LIMITED = Ordered.HIGHEST_PRECEDENCE + 11

    /** 중복 요청 스킵. 레이트리밋과 분산 락(+12)을 통과한 것만 본다. */
    const val DEDUPLICATED = Ordered.HIGHEST_PRECEDENCE + 13

    /** 이전 결과 재사용. 중복 판정 이후여야 "같은 요청"의 정의가 일관된다. 캐시와 같은 값 — 위 설명 참고. */
    const val IDEMPOTENT = Ordered.HIGHEST_PRECEDENCE + 14

    /**
     * 읽기 캐시와 무효화. 둘은 한 메서드에 같이 붙을 일이 없어(하나는 읽기, 하나는 쓰기)
     * 같은 값을 써도 모호함이 생기지 않는다.
     */
    const val CACHED = Ordered.HIGHEST_PRECEDENCE + 14
    const val CACHE_EVICT = Ordered.HIGHEST_PRECEDENCE + 14

}
