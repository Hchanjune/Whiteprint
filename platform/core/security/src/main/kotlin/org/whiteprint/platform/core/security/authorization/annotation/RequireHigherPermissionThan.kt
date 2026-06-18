package org.whiteprint.platform.core.security.authorization.annotation

import org.whiteprint.platform.core.security.authorization.AuthorizedPermission
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireHigherPermissionThan(
    val permissionClass: KClass<out AuthorizedPermission>,
    val permission: String,
)
