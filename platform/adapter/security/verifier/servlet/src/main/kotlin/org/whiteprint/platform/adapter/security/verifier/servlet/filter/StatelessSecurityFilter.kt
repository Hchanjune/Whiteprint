package org.whiteprint.platform.adapter.security.verifier.servlet.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier

class StatelessSecurityFilter(
    private val serializer: Serializer,
    private val accessTokenVerifier: AccessTokenVerifier,
): OncePerRequestFilter() {

    override fun shouldNotFilterErrorDispatch(): Boolean = true

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)

            require(token != null) {
                throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)
            }

            val claims = accessTokenVerifier.verifyOrThrow(AccessToken(token))

            val authentication = UsernamePasswordAuthenticationToken(
                claims.subject,
                null,
                claims.authorities.map { SimpleGrantedAuthority(it) }
            )
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (exception: SecurityException) {
            returnErrorResponse(response, exception)
        } catch (exception: Exception) {
            returnErrorResponse(response, SecurityException(SecurityPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR))
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (header.startsWith("Bearer ", ignoreCase = true)) {
            return header.substring(7).trim().takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun returnErrorResponse(response: HttpServletResponse, exception: SecurityException) {
        response.status = exception.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorBody = ApiResponse.error(
            id = "-",
            exception = exception,
            traceId = null,
        )

        response.writer.write(serializer.serializeToJson(errorBody))
        response.writer.flush()
    }

}