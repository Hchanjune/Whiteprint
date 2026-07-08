package org.whiteprint.platform.core.cache.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimited(
    val prefix: String = "",
    val limit: Long,
    val ttl: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)
