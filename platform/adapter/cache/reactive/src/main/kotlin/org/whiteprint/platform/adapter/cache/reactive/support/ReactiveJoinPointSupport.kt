package org.whiteprint.platform.adapter.cache.reactive.support

import reactor.core.publisher.Mono

/**
 * Spring AOP 프록시는 suspend 메서드 호출을 이미 Mono로 브릿징해서 넘겨준다
 * (joinPoint.proceed()가 Mono<*>를 반환). 혹시 대상이 진짜 동기 메서드거나 null이어도
 * 안전하게 Mono로 통일해서 다루기 위한 헬퍼.
 */
@Suppress("UNCHECKED_CAST")
internal fun asMono(result: Any?): Mono<Any> = when (result) {
    is Mono<*> -> result as Mono<Any>
    null -> Mono.empty()
    else -> Mono.just(result)
}
