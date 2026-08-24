package org.whiteprint.platform.adapter.security.verifier.servlet.aspect

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.whiteprint.platform.core.security.authorization.Authorizer
import org.whiteprint.platform.core.security.authorization.annotation.ForbidPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireAllPermissions
import org.whiteprint.platform.core.security.authorization.annotation.RequireAnyAudience
import org.whiteprint.platform.core.security.authorization.annotation.RequireAnyPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireAudience
import org.whiteprint.platform.core.security.authorization.annotation.RequireHigherPermissionThan
import org.whiteprint.platform.core.security.authorization.annotation.RequirePermission
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy

@Aspect
class SecurityAuthorizationAspect(
    private val authorizer: Authorizer,
) {

    @Before("@annotation(annotation)")
    fun requirePermission(annotation: RequirePermission) {
        if (!authorizer.requirePermission(annotation.permission)) {
            throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
        }
    }

    @Before("@annotation(annotation)")
    fun forbidPermission(annotation: ForbidPermission) {
        if (!authorizer.forbidPermission(annotation.permission)) {
            throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
        }
    }

    @Before("@annotation(annotation)")
    fun requireAnyPermission(annotation: RequireAnyPermission) {
        if (!authorizer.requireAnyPermission(*annotation.permissions)) {
            throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
        }
    }

    @Before("@annotation(annotation)")
    fun requireAllPermissions(annotation: RequireAllPermissions) {
        if (!authorizer.requireAllPermissions(*annotation.permissions)) {
            throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
        }
    }

    @Before("@annotation(annotation)")
    fun requireAudience(annotation: RequireAudience) {
        if (!authorizer.requireAudience(annotation.audience)) {
            throw SecurityException(SecurityPolicy.IMPROPER_AUDIENCE)
        }
    }

    @Before("@annotation(annotation)")
    fun requireAnyAudience(annotation: RequireAnyAudience) {
        if (!authorizer.requireAnyAudience(*annotation.audiences)) {
            throw SecurityException(SecurityPolicy.IMPROPER_AUDIENCE)
        }
    }

    @Before("@annotation(annotation)")
    fun requireHigherPermissionThan(annotation: RequireHigherPermissionThan) {
        val threshold = annotation.permissionClass.java.enumConstants
            .firstOrNull { it.value == annotation.permission }
            ?: throw SecurityException(SecurityPolicy.PERMISSION_DENIED)

        if (!authorizer.requireHigherPermissionThan(threshold)) {
            throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
        }
    }
}
