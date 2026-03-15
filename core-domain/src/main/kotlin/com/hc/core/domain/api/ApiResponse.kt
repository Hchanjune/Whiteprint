package com.hc.core.domain.api

import java.time.Instant

data class ApiResponse<out T>(
    val id: String,
    val isSuccess: Boolean,
    val message: String,
    val data: T,
    val timestamp: Instant,
    val traceId: String,
) {

    companion object {

        fun <T> success(
            id: String?,
            data: T,
            traceId: String?,
            message: String
        ): ApiResponse<T> {
            return ApiResponse(
                id = id?: "-",
                isSuccess = true,
                message = message,
                data = data,
                timestamp = Instant.now(),
                traceId = traceId?: "-",
            )
        }

        fun error(
            id: String?,
            data: Any? = null,
            traceId: String?,
            exception: Exception
        ): ApiResponse<Any?> {
            return ApiResponse(
                id = id?: "-",
                isSuccess = false,
                message = exception.localizedMessage?: exception.message?: exception::class.simpleName ?: "unknown error",
                data = "Internal Error Occurred",
                timestamp = Instant.now(),
                traceId = traceId?: "-",
            )
        }

    }


}