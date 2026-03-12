package com.hc.infra.security.verifier.policy

import com.hc.core.exception.StandardException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.IncorrectClaimException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException

class JwtException (
    errorCode: JwtPolicies,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(errorCode, attributes, cause) {

    companion object {
        fun mapFrom(exception: Exception): JwtException {
            return when (exception) {
                is JwtException -> exception
                is ExpiredJwtException ->
                    JwtException(JwtPolicies.TOKEN_EXPIRED)
                is SignatureException ->
                    JwtException(JwtPolicies.TOKEN_SIGNATURE_INVALID)
                is MalformedJwtException ->
                    JwtException(JwtPolicies.TOKEN_INVALID)
                is UnsupportedJwtException ->
                    JwtException(JwtPolicies.TOKEN_UNSUPPORTED)
                is IncorrectClaimException ->
                    JwtException(JwtPolicies.TOKEN_CLAIM_INVALID)
                else -> JwtException(JwtPolicies.TOKEN_VERIFICATION_INTERNAL_ERROR)
            }
        }
    }

}