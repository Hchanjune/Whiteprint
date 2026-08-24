package org.whiteprint.platform.adapter.security.verifier.reactive.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.whiteprint.platform.adapter.security.verifier.reactive.security.VerifiedUser
import org.whiteprint.platform.core.security.authorization.AuthorizedPermission
import org.whiteprint.platform.core.security.authorization.annotation.ForbidPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireAllPermissions
import org.whiteprint.platform.core.security.authorization.annotation.RequireAnyAudience
import org.whiteprint.platform.core.security.authorization.annotation.RequireAnyPermission
import org.whiteprint.platform.core.security.authorization.annotation.RequireAudience
import org.whiteprint.platform.core.security.authorization.annotation.RequireHigherPermissionThan
import org.whiteprint.platform.core.security.authorization.annotation.RequirePermission
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

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
    fun requireAudience(jp: ProceedingJoinPoint, annotation: RequireAudience): Any? =
        checkAndProceed(jp, SecurityPolicy.IMPROPER_AUDIENCE) { annotation.audience in it.audience }

    @Around("@annotation(annotation)")
    fun requireAnyAudience(jp: ProceedingJoinPoint, annotation: RequireAnyAudience): Any? =
        checkAndProceed(jp, SecurityPolicy.IMPROPER_AUDIENCE) { claims ->
            annotation.audiences.any { it in claims.audience }
        }

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
        denyPolicy: SecurityPolicy = SecurityPolicy.PERMISSION_DENIED,
        check: (AccessTokenClaims) -> Boolean,
    ): Any? {
        val authCheck = ReactiveSecurityContextHolder.getContext()
            .map { ctx ->
                val claims = (ctx.authentication as? VerifiedUser)?.claims
                    ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)
                if (!check(claims)) throw SecurityException(denyPolicy)
                claims
            }
            // getContext() completes empty when the Reactor context carries no SecurityContext —
            // an annotated method reached without authentication. Left empty, the branches below
            // disagree: the publisher ones return an empty result (no body, no error) and the
            // blocking one gets null from block() and proceeds unchecked. Denying here makes all
            // three answer the same way.
            .switchIfEmpty(Mono.error { SecurityException(SecurityPolicy.TOKEN_NOT_FOUND) })

        val method = (jp.signature as MethodSignature).method

        // proceed() must not run before the check passes, or a denied call still performs its
        // side effects. Which branch can defer it depends on what the target returns:
        @Suppress("UNCHECKED_CAST")
        return when {
            // Declared publisher: proceeding inside the operator keeps the body — including
            // anything the method does eagerly before returning the publisher — behind the check.
            Mono::class.java.isAssignableFrom(method.returnType) ->
                authCheck.flatMap<Any> { jp.proceed() as Mono<Any> }

            Flux::class.java.isAssignableFrom(method.returnType) ->
                authCheck.flatMapMany<Any> { jp.proceed() as Flux<Any> }

            // suspend fun: the JVM return type is Object, and Spring bridges the call to a cold
            // Mono, so proceeding here only assembles it — the body runs on subscription, after
            // the check. The continuation belongs to this call, so it is not deferred.
            isSuspend(method) -> when (val result = jp.proceed()) {
                is Mono<*> -> authCheck.flatMap<Any> { result as Mono<Any> }
                else -> { authCheck.block(); result }
            }

            // Blocking target: nothing is deferred, so the check has to complete first.
            else -> {
                authCheck.block()
                jp.proceed()
            }
        }
    }

    private fun isSuspend(method: Method): Boolean =
        method.parameterTypes.lastOrNull()?.let { Continuation::class.java.isAssignableFrom(it) } == true
}
