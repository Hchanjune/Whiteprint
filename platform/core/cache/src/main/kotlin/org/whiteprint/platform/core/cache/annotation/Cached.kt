package org.whiteprint.platform.core.cache.annotation

import java.util.concurrent.TimeUnit

/**
 * 메서드 결과를 캐시에서 읽고, 없으면 실행 후 저장한다(read-through).
 *
 * ## ⚠ 키에 없는 값에 결과가 좌우되면 안 된다
 * 캐시된 `data` 는 **그대로** 다음 요청에 나간다. 그래서 결과가 요청마다 달라지는 값에 좌우되는데
 * 그 값이 키에 없으면, **남의 계산 결과가 그대로 나간다.**
 *
 * ```kotlin
 * // 위험: 조회자 기준으로 liked / isMyComment 를 계산해 담는 조회
 * @Cached(prefix = "comment:list:v1")   // 키에 viewerId 가 없다면
 * fun search(query: SearchQuery): ...   // 첫 사용자의 liked 가 모두에게 나간다
 * ```
 *
 * 애스펙트는 이걸 막을 수 없다 — 무엇이 결과를 좌우하는지는 붙이는 쪽만 안다.
 * **"이 메서드의 결과를 바꾸는 모든 입력이 키에 있는가"** 를 붙이기 전에 확인할 것.
 *
 * ## 유스케이스(`OperationResult` 반환)에도 붙일 수 있다
 * 반환값이 `OperationResult` 면 `data` 만 저장하고, 히트일 때 **지금 요청의 컨텍스트**로 다시 조립한다
 * (`ManagedContext` 에 traceId·ip·deviceId 가 있어서 통째로 캐싱하면 남의 요청 정보가 박제된다).
 * 판단은 `@ManagedOperation` 유무가 아니라 **반환값의 타입**으로 한다.
 *
 * ⚠ 히트일 때는 **메서드 본문이 실행되지 않으므로** 거기서 채우던 것이 전부 빠진다 —
 * 특히 `Operations { }` 블록에서 세팅하던 `message` 는 기본값으로 남는다.
 * 로그에서 "왜 메시지가 비어 있지?" 로 헤매기 쉬운 자리다.
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
annotation class Cached(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)