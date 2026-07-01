package org.whiteprint.platform.adapter.security.verifier.reactive.filter

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import reactor.core.publisher.Mono

class SecurityServerAuthenticationEntryPoint(
    private val serializer: Serializer,
) : ServerAuthenticationEntryPoint {

    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
        val exception = exchange.attributes[StatelessWebSecurityFilter.SECURITY_EXCEPTION_KEY]
                as? SecurityException
            ?: SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)

        val response = exchange.response
        response.statusCode = HttpStatus.valueOf(exception.status)
        response.headers.contentType = MediaType.APPLICATION_JSON
        response.headers.remove("WWW-Authenticate")

        val body = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            traceId = null,
            exception = exception,
        )
        val bytes = serializer.serializeToBytes(body)
        val buffer = response.bufferFactory().wrap(bytes)

        return response.writeWith(Mono.just(buffer))
            .doOnError { DataBufferUtils.release(buffer) }
    }
}
