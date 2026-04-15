package org.whiteprint.platform.core.kernel.model

import org.whiteprint.platform.core.kernel.policy.exception.StandardException
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
            val message = if (exception is StandardException) {
                exception.message?: "unknown error"
            } else {
                exception.localizedMessage?: exception.message?: exception::class.simpleName ?: "unknown error"
            }
            val code = if (exception is StandardException) {
                exception.code
            } else {
                null
            }
            return ApiResponse(
                id = id?: "-",
                isSuccess = false,
                message = message,
                data = code,
                timestamp = Instant.now(),
                traceId = traceId?: "-",
            )
        }

        fun clientError(
            id: String?,
            message: String,
            traceId: String?,
        ): ApiResponse<Any?> {
            return ApiResponse(
                id = id?: "-",
                isSuccess = false,
                message = message,
                data = null,
                timestamp = Instant.now(),
                traceId = traceId?: "-",
            )
        }

    }


}