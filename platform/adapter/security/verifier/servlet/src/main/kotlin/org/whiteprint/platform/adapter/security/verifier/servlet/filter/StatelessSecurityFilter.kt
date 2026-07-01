package org.whiteprint.platform.adapter.security.verifier.servlet.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter
import org.whiteprint.platform.adapter.security.verifier.servlet.security.PermittedEntryPointProvider
import org.whiteprint.platform.adapter.security.verifier.servlet.security.VerifiedUser
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier

class StatelessSecurityFilter(
    private val securityContextRepository: SecurityContextRepository,
    private val permittedEntryPointProvider: PermittedEntryPointProvider,
    private val accessTokenVerifier: AccessTokenVerifier,
): OncePerRequestFilter() {

    companion object {
        const val SECURITY_EXCEPTION_KEY = "SECURITY_EXCEPTION"
    }

    override fun shouldNotFilterErrorDispatch(): Boolean = true

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val permitAllMatchers = permittedEntryPointProvider.matchers()

        if (permitAllMatchers.any { it.matches(request) }) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = extractToken(request)
                ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)

            val claims = accessTokenVerifier.verifyOrThrow(AccessToken(token))
            val context = SecurityContextHolder.createEmptyContext().apply {
                authentication = VerifiedUser(
                    claims = claims,
                    authorities = claims.permissions.map { SimpleGrantedAuthority(it) }
                )
            }
            SecurityContextHolder.setContext(context)
            securityContextRepository.saveContext(context, request, response)
        } catch (exception: SecurityException) {
            request.setAttribute(SECURITY_EXCEPTION_KEY, exception)
        } catch (exception: Exception) {
            request.setAttribute(SECURITY_EXCEPTION_KEY, SecurityException(
                policy = SecurityPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR,
                cause = exception
            ))
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val headerValue = request.getHeader(accessTokenVerifier.headerName) ?: return null

        val scheme = accessTokenVerifier.scheme
        val prefix = "$scheme "

        if (headerValue.startsWith(prefix, ignoreCase = true)) {
            return headerValue
                .substring(prefix.length)
                .trim()
                .takeIf { it.isNotBlank() }
        }

        return null
    }



}