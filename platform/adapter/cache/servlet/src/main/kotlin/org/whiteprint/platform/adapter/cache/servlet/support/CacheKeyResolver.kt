package org.whiteprint.platform.adapter.cache.servlet.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature

/**
 * `@CachedKey`/`@DeduplicatedKey`/`@RateLimitedKey`/`@CacheEvictKey`/`@IdempotentKey` 처럼
 * "파라미터 or 필드에 붙는 order 달린 키 마커 애노테이션" 공통 처리.
 * DistributedLockAspect.buildLockKey()와 동일한 스캔 방식을 여러 애노테이션 타입에 재사용한다.
 */
internal object CacheKeyResolver {

    fun <A : Annotation> resolve(
        joinPoint: ProceedingJoinPoint,
        annotationClass: Class<A>,
        orderOf: (A) -> Int,
    ): List<Pair<Int, String>> {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args
        val params = method.parameters

        val entries = mutableListOf<Pair<Int, String>>()

        params.forEachIndexed { i, param ->
            param.getAnnotation(annotationClass)?.let { annotation ->
                args[i]?.let { entries.add(orderOf(annotation) to it.toString()) }
            }
        }

        if (entries.isEmpty()) {
            args.forEach { arg ->
                if (arg == null) return@forEach
                arg::class.java.declaredFields.forEach { field ->
                    field.getAnnotation(annotationClass)?.let { annotation ->
                        field.isAccessible = true
                        field.get(arg)?.let { entries.add(orderOf(annotation) to it.toString()) }
                    }
                }
            }
        }

        return entries
    }

    /** order가 같으면(기본값 0 포함) 선언 순서를 유지한다 (stable sort). */
    fun buildKeyPart(entries: List<Pair<Int, String>>): String =
        if (entries.size == 1) entries[0].second
        else entries.sortedBy { it.first }.joinToString(":") { it.second }

}
