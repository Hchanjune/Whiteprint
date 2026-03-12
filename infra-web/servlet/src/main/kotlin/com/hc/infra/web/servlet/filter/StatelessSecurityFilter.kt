package com.hc.infra.web.servlet.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.hc.core.api.ApiResponse
import com.hc.infra.security.verifier.model.AccessToken
import com.hc.infra.security.verifier.policy.JwtException
import com.hc.infra.security.verifier.policy.TokenPolicy
import com.hc.infra.security.verifier.service.AccessTokenVerifier
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class StatelessSecurityFilter(
    private val objectMapper: ObjectMapper,
    private val accessTokenVerifier: AccessTokenVerifier
): OncePerRequestFilter() {

    override fun shouldNotFilterErrorDispatch(): Boolean = true

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("activated")
        try {
            val token = extractToken(request)
            if (token != null) {
                val claims = accessTokenVerifier.verifyOrThrow(AccessToken(token))
                val authentication = UsernamePasswordAuthenticationToken(
                    claims.subject,
                    null,
                    claims.authorities.map { SimpleGrantedAuthority(it) }
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
            filterChain.doFilter(request, response)
        } catch (exception: JwtException) {
            returnErrorResponse(response, exception)
        } catch (exception: Exception) {
            returnErrorResponse(response, JwtException(TokenPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR))
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (header.startsWith("Bearer ", ignoreCase = true)) {
            return header.substring(7).trim().takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun returnErrorResponse(response: HttpServletResponse, exception: JwtException) {
        response.status = exception.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorBody = ApiResponse.error(
            id = "-",
            exception = exception,
            traceId = null,
        )

        response.writer.write(objectMapper.writeValueAsString(errorBody))
        response.writer.flush()
    }

}