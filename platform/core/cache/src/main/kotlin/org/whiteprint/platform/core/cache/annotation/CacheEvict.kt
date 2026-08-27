package org.whiteprint.platform.core.cache.annotation

/**
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
annotation class CacheEvict(
    val prefix: String = "",
    /** true면 메서드 실행 전에 evict, false(기본)면 성공적으로 실행된 후 evict. */
    val beforeInvocation: Boolean = false
)
