package org.whiteprint.platform.core.lock.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val prefix: String = "",
    val ttl: Long = 5000L,
    val wait: Long = 3000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)
