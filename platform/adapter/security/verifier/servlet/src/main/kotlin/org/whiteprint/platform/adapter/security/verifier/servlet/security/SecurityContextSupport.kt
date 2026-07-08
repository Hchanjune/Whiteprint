package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.security.core.context.SecurityContextHolder
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy

object SecurityContextSupport {
    fun getCurrentClaims(): AccessTokenClaims {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth is VerifiedUser) {
            return auth.claims
        }

        val principal = auth?.principal
        if (principal is AccessTokenClaims) {
            return principal
        }

        throw SecurityException(policy = SecurityPolicy.TOKEN_NOT_FOUND)
    }

    fun getCurrentSubject(): String = SecurityContextHolder.getContext().authentication?.name
        ?: throw SecurityException(policy = SecurityPolicy.TOKEN_NOT_FOUND)
}