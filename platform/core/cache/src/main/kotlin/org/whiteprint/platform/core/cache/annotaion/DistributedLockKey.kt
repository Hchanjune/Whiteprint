package org.whiteprint.platform.core.cache.annotaion

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLockKey