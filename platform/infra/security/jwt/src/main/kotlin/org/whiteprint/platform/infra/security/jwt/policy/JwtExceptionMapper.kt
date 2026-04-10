package org.whiteprint.platform.infra.security.jwt.policy

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.IncorrectClaimException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy

object JwtExceptionMapper {
    fun mapFrom(exception: Exception): SecurityException {
        return when (exception) {
            is SecurityException -> exception
            is ExpiredJwtException ->
                SecurityException(SecurityPolicy.TOKEN_EXPIRED)
            is SignatureException ->
                SecurityException(SecurityPolicy.TOKEN_SIGNATURE_INVALID)
            is MalformedJwtException ->
                SecurityException(SecurityPolicy.TOKEN_INVALID)
            is UnsupportedJwtException ->
                SecurityException(SecurityPolicy.TOKEN_UNSUPPORTED)
            is IncorrectClaimException ->
                SecurityException(SecurityPolicy.TOKEN_CLAIM_INVALID)
            else -> SecurityException(SecurityPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR, cause = exception)
        }
    }
}