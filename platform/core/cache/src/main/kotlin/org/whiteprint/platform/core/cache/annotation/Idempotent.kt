package org.whiteprint.platform.core.cache.annotation

import java.util.concurrent.TimeUnit

/**
 * 같은 요청이면 이전 결과를 그대로 돌려준다. 저장·복원 방식은 `@Cached` 와 동일하다 —
 * `OperationResult` 는 `data` 만 저장되고 히트 시 현재 요청 컨텍스트로 다시 조립된다.
 *
 * ⚠ `@Cached` 와 같은 함정을 공유한다: **키에 없는 값에 결과가 좌우되면 남의 결과가 나간다**,
 * 그리고 **히트일 때는 본문이 안 돌아서 `message` 가 비어 있다**. 자세한 것은 `@Cached` 참고.
 *
 * ## prefix 규약: `<도메인>:<대상>:v<n>`
 *
 * **prefix 끝에 버전을 붙인다** — 예: `notification:status:v1`.
 *
 * 키를 이루는 조각이 늘거나 줄면 키 문자열이 통째로 달라진다. 그러면 기존 Redis 키가 전부 고아가 되고,
 * 배포 직후 한동안 전량 미스가 난다. 더 나쁜 경우는 형식이 바뀐 줄 모르고 **옛 형식의 값을 새 코드가
 * 읽어버리는 것**이다.
 *
 * 버전을 붙여두면 형식을 바꿀 때 `v1` → `v2` 로 올리기만 하면 된다 — 옛 키는 TTL 로 자연히 사라지고
 * 두 형식이 섞이지 않는다.
 *
 * ⚠ `@Cached` 와 `@CacheEvict` 는 **prefix 가 정확히 같아야 한다**(버전 포함).
 * 다르면 무효화가 걸리지 않는데, 그건 기동 시점 검사가 잡지 못한다 — 검사는 키 **이름 구성**만 대조하고
 * prefix 가 다르면 애초에 같은 캐시로 취급하지 않기 때문이다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)
