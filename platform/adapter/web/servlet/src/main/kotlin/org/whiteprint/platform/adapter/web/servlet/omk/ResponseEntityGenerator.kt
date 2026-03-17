package org.whiteprint.platform.adapter.web.servlet.omk

import org.whiteprint.platform.core.kernel.model.ApiResponse
import io.github.hchanjune.omk.core.OperationResult
import org.springframework.http.ResponseEntity

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

}