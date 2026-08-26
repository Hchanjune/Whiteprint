package org.whiteprint.platform.adapter.cache.servlet.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method

/**
 * `@CachedKey`/`@DeduplicatedKey`/`@RateLimitedKey`/`@CacheEvictKey`/`@IdempotentKey` 처럼
 * "파라미터 or 필드에 붙는 이름 달린 키 마커 애노테이션" 공통 처리.
 *
 * 키는 **이름순으로 정렬**해 `이름=값:이름=값` 으로 만든다.
 * 선언 순서를 쓰지 않는 이유는 두 가지다:
 * - 필드를 위아래로 옮기는 리팩터링만으로 키가 바뀌면 안 된다
 * - `@Cached` 와 `@CacheEvict` 는 보통 **다른 클래스**에 있어서, 위치로는 서로를 못 알아본다
 *
 * 값 하나짜리도 `이름=값` 형식을 그대로 쓴다 — 조각이 하나에서 둘로 늘어날 때
 * 기존 조각의 표현이 바뀌지 않아야 하기 때문이다.
 */
internal object CacheKeyResolver {

    /**
     * 키 조각을 이름과 함께 뽑는다. 이름이 비어 있으면 파라미터/필드 이름을 쓴다.
     *
     * ⚠ 파라미터 애노테이션이 하나라도 있으면 필드 스캔을 하지 않는다 — 섞어 쓰면 필드 쪽이 조용히 무시된다.
     * ⚠ `declaredFields` 라 상위 클래스에 선언된 필드는 잡지 않는다.
     * 둘 다 기동 검사(`CacheKeyContractValidator`)가 잡아준다.
     */
    fun <A : Annotation> resolve(
        joinPoint: ProceedingJoinPoint,
        annotationClass: Class<A>,
        nameOf: (A) -> String,
    ): List<Pair<String, String>> {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args

        val entries = mutableListOf<Pair<String, String>>()

        method.parameters.forEachIndexed { i, param ->
            param.getAnnotation(annotationClass)?.let { annotation ->
                args[i]?.let { entries.add(nameOf(annotation).ifBlank { param.name } to it.toString()) }
            }
        }

        if (entries.isEmpty()) {
            args.forEach { arg ->
                if (arg == null) return@forEach
                arg::class.java.declaredFields.forEach { field ->
                    field.getAnnotation(annotationClass)?.let { annotation ->
                        field.isAccessible = true
                        field.get(arg)?.let { entries.add(nameOf(annotation).ifBlank { field.name } to it.toString()) }
                    }
                }
            }
        }

        return entries
    }

    /**
     * 메서드 시그니처만으로 키 **이름**을 뽑는다(값 없이). 기동 시점 계약 검사용이다.
     * 실행 시 [resolve] 가 고르는 경로(파라미터 우선, 없으면 인자 타입의 필드)와 같은 규칙이어야 한다.
     */
    fun <A : Annotation> resolveNames(
        method: Method,
        annotationClass: Class<A>,
        nameOf: (A) -> String,
    ): List<String> {
        val fromParameters = method.parameters.mapNotNull { param ->
            param.getAnnotation(annotationClass)?.let { nameOf(it).ifBlank { param.name } }
        }
        if (fromParameters.isNotEmpty()) return fromParameters.sorted()

        return method.parameterTypes
            .flatMap { it.declaredFields.asIterable() }
            .mapNotNull { field ->
                field.getAnnotation(annotationClass)?.let { nameOf(it).ifBlank { field.name } }
            }
            .sorted()
    }

    fun buildKeyPart(entries: List<Pair<String, String>>): String =
        entries.sortedBy { it.first }.joinToString(":") { "${it.first}=${it.second}" }

}
