package org.whiteprint.platform.adapter.web.servlet.omk

import org.whiteprint.platform.core.kernel.model.ApiResponse
import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.servlet.Operations
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.policy.exception.StandardException

object ResponseEntityGenerator {

    fun <RESULT : Any, RESPONSE : Any> generateFromOperation(
        operationResult: OperationResult<RESULT>,
        cookie: ResponseCookie? = null,
        mapper: (RESULT) -> RESPONSE,
    ): ResponseEntity<ApiResponse<RESPONSE>> {

        val apiResponse = ApiResponse.success(
            id = operationResult.context.traceId,
            data = mapper(operationResult.data),
            traceId = operationResult.context.traceId,
            message = operationResult.context.message,
        )
        val response = ResponseEntity.ok()
        cookie?.let {
            response.header(SET_COOKIE, cookie.toString())
        }
        return response.body(apiResponse)
    }

    fun <T> generateInstantData(data: T, message: String = ""): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse.success(
            id = TsidGenerator.generate().toString(),
            data = data,
            traceId = Operations.context.traceId,
            message = message,
        )
        return ResponseEntity.ok(response)
    }

    fun generateFromHandledException(exception: StandardException): ResponseEntity<ApiResponse<Any?>> {
        val response = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            traceId = Operations.context.traceId,
            exception = exception,
        )
        return ResponseEntity.status(exception.status).body(response)
    }

}