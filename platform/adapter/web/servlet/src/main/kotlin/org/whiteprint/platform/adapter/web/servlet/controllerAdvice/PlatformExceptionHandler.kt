package org.whiteprint.platform.adapter.web.servlet.controllerAdvice

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.whiteprint.platform.adapter.web.servlet.omk.ResponseEntityGenerator
import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException
import org.whiteprint.platform.core.kernel.policy.exception.DomainValidationException

@RestControllerAdvice
class PlatformExceptionHandler {

    @ExceptionHandler(DomainValidationException::class)
    fun handleValidationException(exception: DomainValidationException) =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(DomainPolicyException::class)
    fun handleValidationException(exception: DomainPolicyException) =
        ResponseEntityGenerator.generateFromHandledException(exception)

}