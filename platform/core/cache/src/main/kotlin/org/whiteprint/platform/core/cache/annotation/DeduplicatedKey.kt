package org.whiteprint.platform.core.cache.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeduplicatedKey (
    val order: Int = 0
)
