package org.whiteprint.platform.adapter.security.verifier.reactive.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.whiteprint.platform.adapter.security.verifier.reactive.security.VerifiedUser
import org.whiteprint.platform.core.security.authorization.AuthorizedPermission
import org.whiteprint.platform.core.security.authorization.annotation.ForbidPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireAllPermissions
import org.whiteprint.platform.core.security.authorization.annotation.RequireAnyPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireHigherPermissionThan
import org.whiteprint.platform.core.security.authorization.annotation.RequirePermission
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive-aware authorization aspect.
 *
 * Uses @Around to wrap the returned Mono/Flux with a permission check sourced from the
 * Reactor Context set by StatelessWebSecurityFilter. The check runs inside the publisher
 * chain so it has access to the security context without blocking.
 */
@Aspect
class SecurityAuthorizationAspect {

    @Around("@annotation(annotation)")
    fun requirePermission(jp: ProceedingJoinPoint, annotation: RequirePermission): Any? =
        checkAndProceed(jp) { annotation.permission in it.permissions }

    @Around("@annotation(annotation)")
    fun forbidPermission(jp: ProceedingJoinPoint, annotation: ForbidPermission): Any? =
        checkAndProceed(jp) { annotation.permission !in it.permissions }

    @Around("@annotation(annotation)")
    fun requireAnyPermission(jp: ProceedingJoinPoint, annotation: RequireAnyPermission): Any? =
        checkAndProceed(jp) { claims -> annotation.permissions.any { it in claims.permissions } }

    @Around("@annotation(annotation)")
    fun requireAllPermissions(jp: ProceedingJoinPoint, annotation: RequireAllPermissions): Any? =
        checkAndProceed(jp) { claims -> annotation.permissions.all { it in claims.permissions } }

    @Around("@annotation(annotation)")
    fun requireHigherPermissionThan(jp: ProceedingJoinPoint, annotation: RequireHigherPermissionThan): Any? {
        val threshold: AuthorizedPermission = annotation.permissionClass.java.enumConstants
            .firstOrNull { it.value == annotation.permission }
            ?: throw SecurityException(SecurityPolicy.PERMISSION_DENIED)

        return checkAndProceed(jp) { claims ->
            val siblings = threshold.javaClass.enumConstants?.toList() ?: emptyList()
            val highestHeld = claims.permissions
                .mapNotNull { name -> siblings.find { it.value == name } }
                .maxByOrNull { it.priority }
                ?: return@checkAndProceed false
            highestHeld.priority > threshold.priority
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkAndProceed(
        jp: ProceedingJoinPoint,
        check: (AccessTokenClaims) -> Boolean,
    ): Any? {
        val authCheck = ReactiveSecurityContextHolder.getContext()
            .map { ctx ->
                val claims = (ctx.authentication as? VerifiedUser)?.claims
                    ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)
                if (!check(claims)) throw SecurityException(SecurityPolicy.PERMISSION_DENIED)
                claims
            }

        @Suppress("UNCHECKED_CAST")
        return when (val result = jp.proceed()) {
            is Mono<*> -> authCheck.flatMap<Any> { result as Mono<Any> }
            is Flux<*> -> authCheck.flatMapMany<Any> { result as Flux<Any> }
            else -> {
                authCheck.block()
                result
            }
        }
    }
}
