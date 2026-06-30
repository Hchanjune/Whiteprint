package org.whiteprint.platform.adapter.web.reactive.omk

import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.webflux.ReactiveOperations
import org.springframework.http.ResponseEntity
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.policy.exception.StandardException
import reactor.core.publisher.Mono

object ResponseEntityGenerator {

    fun <T> generateInstantData(data: T, message: String = ""): Mono<ResponseEntity<ApiResponse<T>>> =
        Mono.deferContextual { ctx ->
            val traceId = ctx.getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY).orElse(null)?.traceId
            val response = ApiResponse.success(
                id = TsidGenerator.generate().toString(),
                data = data,
                traceId = traceId,
                message = message,
            )
            Mono.just(ResponseEntity.ok(response))
        }

    fun generateFromHandledException(exception: StandardException): Mono<ResponseEntity<ApiResponse<Any?>>> =
        Mono.deferContextual { ctx ->
            val traceId = ctx.getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY).orElse(null)?.traceId
            val response = ApiResponse.error(
                id = TsidGenerator.generate().toString(),
                traceId = traceId,
                exception = exception,
            )
            Mono.just(ResponseEntity.status(exception.status).body(response))
        }
}
