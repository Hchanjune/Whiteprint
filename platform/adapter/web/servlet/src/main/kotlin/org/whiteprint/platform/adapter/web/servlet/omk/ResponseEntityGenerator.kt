package org.whiteprint.platform.adapter.web.servlet.omk

import org.whiteprint.platform.core.kernel.model.ApiResponse
import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.http.ResponseEntity
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.policy.exception.StandardException

object ResponseEntityGenerator {

    fun <T: Any> generateFromOperation(operationResult: OperationResult<T>): ResponseEntity<ApiResponse<T>> {
        val apiResponse = ApiResponse.success(
            id = operationResult.context.traceId,
            data = operationResult.data,
            traceId = operationResult.context.traceId,
            message = operationResult.context.message,
        )
        return ResponseEntity.ok(apiResponse)
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