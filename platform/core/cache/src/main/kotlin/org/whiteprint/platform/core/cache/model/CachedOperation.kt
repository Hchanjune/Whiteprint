package org.whiteprint.platform.core.cache.model

/**
 * 유스케이스의 `OperationResult` 를 캐싱할 때 **실제로 저장되는 형태** — `data` 만 담는다.
 *
 * ## 왜 봉투가 필요한가
 * `OperationResult(context, data)` 를 통째로 캐싱하면 안 된다. `context`(`ManagedContext`)에는
 * **traceId · causationId · ip · deviceId · entrypoint · statusCode · message · 타이밍**이 들어 있다.
 * 그대로 저장하면 첫 요청의 값이 박제되어 이후 요청들에 그대로 실린다 — 트레이스가 거짓말을 하는 정도가
 * 아니라 **남의 IP 가 남는다**. 게다가 `ManagedContext` 는 생성자에 `Clock`·`SpanIdProvider` 를 받아
 * 직렬화 자체가 실패할 수 있다.
 *
 * ## 왜 봉투 없이는 안 되나
 * `data` 만 저장하면 저장은 되는데, **꺼낼 때 다시 감싸야 하는지 알 수 없다.** 반환 타입으로 판별하려 해도
 * suspend 함수는 JVM 시그니처가 `Object` 라 알아낼 수 없다. 그래서 "이건 감싸야 하는 값"이라는 사실을
 * 캐시에 함께 저장한다.
 *
 * 꺼낼 때는 **지금 들어온 요청의 컨텍스트**로 다시 조립하므로, 캐시된 것은 순수 데이터뿐이고
 * 관측 정보는 언제나 현재 요청의 것이다.
 */
data class CachedOperation(
    val data: Any,
)
