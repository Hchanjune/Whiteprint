package org.whiteprint.platform.core.cache.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheEvict(
    val prefix: String = "",
    /** true면 메서드 실행 전에 evict, false(기본)면 성공적으로 실행된 후 evict. */
    val beforeInvocation: Boolean = false
)
