package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.security.policy.SecurityException
import reactor.core.publisher.Mono

@RestControllerAdvice
class SecurityExceptionHandler {

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(exception: SecurityException): Mono<ResponseEntity<ApiResponse<Any?>>> {
        val response = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            traceId = null,
            exception = exception,
        )
        return Mono.just(ResponseEntity.status(exception.status).body(response))
    }
}
