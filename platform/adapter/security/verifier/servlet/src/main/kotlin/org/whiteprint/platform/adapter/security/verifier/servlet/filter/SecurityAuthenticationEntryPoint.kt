package org.whiteprint.platform.adapter.security.verifier.servlet.filter

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy

class SecurityAuthenticationEntryPoint(
    private val serializer: Serializer,
): AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val exception = request.getAttribute(StatelessSecurityFilter.SECURITY_EXCEPTION_KEY)
                as? SecurityException
            ?: SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)

        returnErrorResponse(response, exception)
    }

    private fun returnErrorResponse(response: HttpServletResponse, exception: SecurityException) {
        response.status = exception.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorBody = ApiResponse.error(
            id = TsidGenerator.generate().toString(),
            exception = exception,
            traceId = null,
        )

        response.writer.write(serializer.serializeToJson(errorBody))
        response.writer.flush()
    }

}