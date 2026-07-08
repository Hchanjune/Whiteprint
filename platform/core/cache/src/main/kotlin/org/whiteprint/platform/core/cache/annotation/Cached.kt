package org.whiteprint.platform.core.cache.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cached(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)