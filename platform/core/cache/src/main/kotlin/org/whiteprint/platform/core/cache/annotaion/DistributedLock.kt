package org.whiteprint.platform.core.cache.annotaion

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val prefix: String = "",
    val ttlMillis: Long = 5000L,
    val waitMillis: Long = 3000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)