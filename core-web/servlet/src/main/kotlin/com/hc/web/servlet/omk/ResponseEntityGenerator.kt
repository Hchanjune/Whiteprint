package com.hc.web.servlet.omk

import com.hc.core.api.ApiResponse
import io.github.hchanjune.omk.core.models.OperationResult
import org.springframework.http.ResponseEntity

object ResponseEntityGenerator {

    fun <T: Any> generateFromOperation(operationResult: OperationResult<T>): ResponseEntity<ApiResponse<T>> {
        val apiResponse = ApiResponse.success(
            id = operationResult.telemetry.traceId,
            data = operationResult.data,
            traceId = operationResult.telemetry.traceId,
            message = operationResult.operation.message,
        )
        return ResponseEntity.ok(apiResponse)
    }

}