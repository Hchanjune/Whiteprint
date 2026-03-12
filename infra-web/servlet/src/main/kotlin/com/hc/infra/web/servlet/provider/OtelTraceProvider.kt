package com.hc.infra.web.servlet.provider

import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component

@Component
class OtelTraceProvider(
    private val tracer: Tracer
) {

    val traceId: String?
        get() = tracer.currentSpan()?.context()?.traceId()

}