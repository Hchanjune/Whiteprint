package org.whiteprint.platform.core.cache.annotation

/**
 * 캐시 키를 이루는 조각. **이름으로 식별한다** — 비워두면 필드/파라미터 이름을 그대로 쓴다.
 *
 * 키는 이름순으로 정렬해 `prefix:이름=값:이름=값` 으로 만들어진다.
 * 선언 순서가 아니라 이름순인 이유: 필드를 위아래로 옮기는 것만으로 키가 바뀌면 안 되고,
 * 서로 다른 클래스가 만든 키가 이름만 맞으면 일치해야 하기 때문이다
 * (`@Cached` 와 `@CacheEvict` 는 보통 다른 클래스에 있다).
 *
 * 이름이 다르면 키도 달라진다. 그 불일치는 기동 시점 검사가 잡는다.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimitedKey(
    val name: String = ""
)
