package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.security.policy.SecurityException

@RestControllerAdvice
class SecurityExceptionHandler {

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(exception: SecurityException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            exception = exception,
        )
        return ResponseEntity.status(exception.status).body(response)
    }

}