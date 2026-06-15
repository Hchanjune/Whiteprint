package org.whiteprint.platform.adapter.web.servlet.configurations

import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.whiteprint.platform.adapter.web.servlet.omk.ResponseEntityGenerator
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException
import org.whiteprint.platform.core.kernel.policy.exception.DomainValidationException
import org.whiteprint.platform.core.kernel.policy.exception.StandardException
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class PlatformExceptionHandler {

    @ExceptionHandler(DomainValidationException::class)
    fun handleValidationException(exception: DomainValidationException) =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(DomainPolicyException::class)
    fun handleValidationException(exception: DomainPolicyException) =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(StandardException::class)
    fun handleStandardException(exception: StandardException) =
        ResponseEntityGenerator.generateFromHandledException(exception)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(
        exception: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Any?>> {
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

        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = message,
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // Unknown Url
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(exception: NoResourceFoundException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "[METHOD: ${exception.httpMethod}]Resource not found: ${exception.resourcePath}",
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    // GET 엔드포인트에 POST로 보내는 등
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Any?>> {
        val supported = exception.supportedMethods?.joinToString(", ") ?: "unknown"
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Method '${exception.method}' not supported. Supported: $supported",
        )
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    // @RequestParam Missing
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(exception: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Missing required parameter: '${exception.parameterName}' (${exception.parameterType})",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // @PathVariable Type Error
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Invalid value '${exception.value}' for parameter '${exception.name}'",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // Content-Type Error
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(exception: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Any?>> {
        val supported = exception.supportedMediaTypes.joinToString(", ")
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Content type '${exception.contentType}' not supported. Supported: $supported",
        )
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response)
    }

    // @Valid (@RequestBody  Bean Validation Failure)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any?>> {
        val errors = exception.bindingResult.fieldErrors.joinToString(", ") {
            "'${it.field}': ${it.defaultMessage}"
        }
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Validation failed: $errors",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // @RequestPart Missing (multipart Missing)
    @ExceptionHandler(MissingServletRequestPartException::class)
    fun handleMissingPart(exception: MissingServletRequestPartException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Missing required part: '${exception.requestPartName}'",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // File size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(exception: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Upload size exceeds the maximum allowed",
        )
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response)
    }

    // @RequestHeader Missing
    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(exception: MissingRequestHeaderException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Missing required header: '${exception.headerName}'",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // @CookieValue Missing
    @ExceptionHandler(MissingRequestCookieException::class)
    fun handleMissingCookie(exception: MissingRequestCookieException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.clientError(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            message = "Missing required cookie: '${exception.cookieName}'",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // UnknownException
    @ExceptionHandler(RuntimeException::class)
    fun handleUnknownException(exception: RuntimeException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            data = "UNKNOWN ERROR OCCURRED",
            exception = exception,
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

}