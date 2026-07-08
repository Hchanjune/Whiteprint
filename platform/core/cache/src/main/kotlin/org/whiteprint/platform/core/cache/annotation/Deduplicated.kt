package org.whiteprint.platform.core.cache.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Deduplicated(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)