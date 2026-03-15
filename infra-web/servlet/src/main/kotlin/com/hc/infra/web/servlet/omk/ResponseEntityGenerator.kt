package com.hc.infra.web.servlet.omk

import com.hc.core.domain.api.ApiResponse
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