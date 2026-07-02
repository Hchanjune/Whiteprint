package org.whiteprint.platform.adapter.web.reactive.configurations

import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.reactive.ReactiveOperations
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException
import org.whiteprint.platform.core.kernel.policy.exception.DomainValidationException
import org.whiteprint.platform.core.kernel.policy.exception.StandardException
import org.whiteprint.platform.adapter.web.reactive.omk.ResponseEntityGenerator
import reactor.core.publisher.Mono
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class PlatformExceptionHandler {

    @ExceptionHandler(DomainValidationException::class)
    fun handleDomainValidation(exception: DomainValidationException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(DomainPolicyException::class)
    fun handleDomainPolicy(exception: DomainPolicyException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(StandardException::class)
    fun handleStandardException(exception: StandardException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        ResponseEntityGenerator.generateFromHandledException(exception)

    // @Valid (@RequestBody Bean Validation Failure)
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(exception: WebExchangeBindException): Mono<ResponseEntity<ApiResponse<Any?>>> {
        val errors = exception.bindingResult.fieldErrors.joinToString(", ") {
            "'${it.field}': ${it.defaultMessage}"
        }
        return clientError(HttpStatus.BAD_REQUEST, "Validation failed: $errors")
    }

    // Body parse errors (Jackson deserialization failure)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(exception: HttpMessageNotReadableException): Mono<ResponseEntity<ApiResponse<Any?>>> {
        val message = when (val cause = exception.cause) {
            is MismatchedInputException -> {
                val path = cause.path.joinToString(".") { ref ->
                    ref.description
                        .substringAfterLast("[\"")
                        .substringBefore("\"]")
                }
                when (cause) {
                    is InvalidFormatException -> {
                        val targetType = cause.targetType
                        if (targetType.isEnum) {
                            val accepted = targetType.enumConstants.joinToString(", ")
                            "Invalid value '${cause.value}' for field '$path'. Accepted values: $accepted"
                        } else {
                            "Invalid value for field '$path'"
                        }
                    }
                    else -> "Invalid or missing value for field '$path'"
                }
            }
            else -> "Http request body is missing or malformed"
        }
        return clientError(HttpStatus.BAD_REQUEST, message)
    }

    // Missing @RequestParam, @RequestHeader, @CookieValue, body required, etc.
    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInput(exception: ServerWebInputException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        clientError(HttpStatus.BAD_REQUEST, exception.reason ?: "Invalid request input")

    // Wrong HTTP method
    @ExceptionHandler(MethodNotAllowedException::class)
    fun handleMethodNotAllowed(exception: MethodNotAllowedException): Mono<ResponseEntity<ApiResponse<Any?>>> {
        val supported = exception.supportedMethods.joinToString(", ") { it.name() }
        return clientError(HttpStatus.METHOD_NOT_ALLOWED, "Method '${exception.httpMethod}' not supported. Supported: $supported")
    }

    // Wrong Content-Type
    @ExceptionHandler(UnsupportedMediaTypeStatusException::class)
    fun handleUnsupportedMediaType(exception: UnsupportedMediaTypeStatusException): Mono<ResponseEntity<ApiResponse<Any?>>> {
        val supported = exception.supportedMediaTypes.joinToString(", ")
        return clientError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type '${exception.contentType}' not supported. Supported: $supported")
    }

    // General Spring WebFlux status exceptions (404, etc.)
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(exception: ResponseStatusException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        clientError(HttpStatus.valueOf(exception.statusCode.value()), exception.reason ?: exception.message)

    // Optimistic Lock (concurrent modification)
    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(exception: OptimisticLockingFailureException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        clientError(HttpStatus.CONFLICT, "Resource was modified by another request. Please retry.")

    // Unknown
    @ExceptionHandler(RuntimeException::class)
    fun handleUnknown(exception: RuntimeException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        Mono.deferContextual { ctx ->
            val traceId = ctx.getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY).orElse(null)?.traceId
            val response = ApiResponse.error(
                id = TsidGenerator.generate().toString(),
                traceId = traceId,
                data = "UNKNOWN ERROR OCCURRED",
                exception = exception,
            )
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response))
        }

    private fun clientError(status: HttpStatus, message: String): Mono<ResponseEntity<ApiResponse<Any?>>> =
        Mono.deferContextual { ctx ->
            val traceId = ctx.getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY).orElse(null)?.traceId
            val response = ApiResponse.clientError(
                id = TsidGenerator.generate().toString(),
                traceId = traceId,
                message = message,
            )
            Mono.just(ResponseEntity.status(status).body(response))
        }
}
