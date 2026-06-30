package org.whiteprint.platform.adapter.web.reactive.request

import org.springframework.http.server.reactive.ServerHttpRequest
import org.whiteprint.platform.core.kernel.clientContext.ClientContext
import org.whiteprint.platform.core.kernel.clientContext.ClientType
import org.whiteprint.platform.core.kernel.clientContext.IP
import org.whiteprint.platform.core.kernel.clientContext.PlatformType

class ClientContextResolver(
    private val platformHeader: String = "X-Platform",
    private val clientTypeHeader: String = "X-Client-Type",
) {

    fun resolve(request: ServerHttpRequest): ClientContext {
        return ClientContext(
            ip = extractIpInfo(request),
            type = resolveClientType(request),
            platform = resolvePlatformType(request)
        )
    }

    private fun extractIpInfo(request: ServerHttpRequest): IP {
        val forwarded = request.headers.getFirst("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            return IP(forwarded.split(",").first().trim())
        }
        return IP(request.remoteAddress?.address?.hostAddress ?: "unknown")
    }

    private fun resolveClientType(request: ServerHttpRequest): ClientType {
        val header = request.headers.getFirst(clientTypeHeader)?.trim()?.uppercase()
        return runCatching { ClientType.valueOf(header ?: "") }.getOrDefault(ClientType.WEB)
    }

    private fun resolvePlatformType(request: ServerHttpRequest): PlatformType {
        val header = request.headers.getFirst(platformHeader)?.trim()?.uppercase()
        return runCatching { PlatformType.valueOf(header ?: "") }.getOrDefault(PlatformType.UNKNOWN)
    }
}
