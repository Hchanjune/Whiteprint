package org.whiteprint.platform.core.security.authorization.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireAllPermissions(val permissions: Array<String>)
