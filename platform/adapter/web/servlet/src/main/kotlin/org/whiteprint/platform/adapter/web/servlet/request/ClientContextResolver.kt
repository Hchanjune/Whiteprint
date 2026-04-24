package org.whiteprint.platform.adapter.web.servlet.request

import jakarta.servlet.http.HttpServletRequest
import org.whiteprint.platform.core.kernel.clientContext.ClientContext
import org.whiteprint.platform.core.kernel.clientContext.ClientType
import org.whiteprint.platform.core.kernel.clientContext.IP
import org.whiteprint.platform.core.kernel.clientContext.PlatformType

class ClientContextResolver(
    private val platformHeader: String = "X-Platform",
    private val clientTypeHeader: String = "X-Client-Type",
) {

    fun resolve(request: HttpServletRequest): ClientContext {
        return ClientContext(
            ip = extractIpInfoFromRequest(request),
            type = resolveClientType(request),
            platform = resolvePlatformType(request)
        )
    }

    private fun extractIpInfoFromRequest(request: HttpServletRequest): IP {
        val forwarded = request.getHeader("X-Forwarded-For")

        if (!forwarded.isNullOrBlank()) {
            val ip = forwarded.split(",").first().trim()
            return IP(ip)
        }

        return IP(request.remoteAddr)
    }

    private fun resolveClientType(request: HttpServletRequest): ClientType {
        val header = request.getHeader(clientTypeHeader)
            ?.trim()
            ?.uppercase()

        return runCatching { ClientType.valueOf(header ?: "") }
            .getOrDefault(ClientType.WEB)
    }

    private fun resolvePlatformType(request: HttpServletRequest): PlatformType {
        val header = request.getHeader(platformHeader)
            ?.trim()
            ?.uppercase()
        return runCatching { PlatformType.valueOf(header ?: "") }
            .getOrDefault(PlatformType.UNKNOWN)
    }

}