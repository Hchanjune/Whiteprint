package org.whiteprint.platform.adapter.lock.distributed.servlet.aspect

import org.springframework.core.Ordered

/**
 * 분산 락 애스펙트의 실행 순서. **작을수록 바깥**이다.
 *
 * ```
 * HIGHEST + 10  ManagedOperation (OMK)     ← 락 획득·해제 스팬이 유스케이스 트레이스 안에 남게
 * HIGHEST + 11  RateLimited
 * HIGHEST + 12  DistributedLock            ← 여기
 * HIGHEST + 13  Deduplicated
 * HIGHEST + 14  Idempotent / Cached / CacheEvict
 * LOWEST        @Transactional (기본값)
 * ```
 *
 * ## 반드시 트랜잭션 바깥이어야 한다
 * `@Order` 가 없으면 `LOWEST_PRECEDENCE` 라 `@Transactional` 과 같은 값이 되어 순서가 임의로 정해진다.
 * 락이 트랜잭션 **안쪽**이 되면 커밋 **전에** 락이 풀리고, 기다리던 다음 요청이 커밋되지 않은 옛 상태를 읽어 같은 일을 또 한다.
 *
 * 캐시 애스펙트와의 관계는 `adapter:cache:common` 의 `CacheAspectOrder` 를 볼 것 — 이 모듈이 그쪽에 의존하지 않아 값만 맞춘다.
 */
object DistributedLockAspectOrder {

    const val DISTRIBUTED_LOCK = Ordered.HIGHEST_PRECEDENCE + 12

}
