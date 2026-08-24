package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.security.authorization.Authorizer
import org.whiteprint.platform.core.security.authorization.AuthorizedPermission

class AuthorizerImpl: Authorizer {

    override fun requirePermission(name: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().permissions
        return name in held
    }

    override fun forbidPermission(name: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().permissions
        return name !in held
    }

    override fun requireAnyPermission(vararg names: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().permissions
        return names.any { it in held }
    }

    override fun requireAllPermissions(vararg names: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().permissions
        return names.all { it in held }
    }

    override fun requireAudience(audience: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().audience
        return audience in held
    }

    override fun requireAnyAudience(vararg audiences: String): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().audience
        return audiences.any { it in held }
    }

    override fun requireHigherPermissionThan(permission: AuthorizedPermission): Boolean {
        val held = SecurityContextSupport.getCurrentClaims().permissions
        val siblings = permission.javaClass.enumConstants?.toList() ?: emptyList()

        val highestHeld = held
            .mapNotNull { name -> siblings.find { it.value == name } }
            .maxByOrNull { it.priority }
            ?: return false

        return highestHeld.priority > permission.priority
    }
}
