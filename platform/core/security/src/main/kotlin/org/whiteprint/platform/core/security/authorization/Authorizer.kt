package org.whiteprint.platform.core.security.authorization

interface Authorizer {

    fun requirePermission(name: String): Boolean
    fun requirePermission(permission: AuthorizedPermission): Boolean = requirePermission(permission.value)

    fun forbidPermission(name: String): Boolean
    fun forbidPermission(permission: AuthorizedPermission): Boolean = forbidPermission(permission.value)

    fun requireAnyPermission(vararg names: String): Boolean
    fun requireAnyPermission(vararg permissions: AuthorizedPermission): Boolean =
        requireAnyPermission(*permissions.map { it.value }.toTypedArray())

    fun requireAllPermissions(vararg names: String): Boolean
    fun requireAllPermissions(vararg permissions: AuthorizedPermission): Boolean =
        requireAllPermissions(*permissions.map { it.value }.toTypedArray())

    fun requireHigherPermissionThan(permission: AuthorizedPermission): Boolean

    fun requireAudience(audience: String): Boolean

    fun requireAnyAudience(vararg audiences: String): Boolean

}
