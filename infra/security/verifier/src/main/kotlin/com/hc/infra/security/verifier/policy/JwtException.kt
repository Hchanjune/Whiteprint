package com.hc.infra.security.verifier.policy

import com.hc.core.kernel.policy.exception.StandardException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.IncorrectClaimException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException

class JwtException (
    errorCode: TokenPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(errorCode, attributes, cause) {

    companion object {
        fun mapFrom(exception: Exception): JwtException {
            return when (exception) {
                is JwtException -> exception
                is ExpiredJwtException ->
                    JwtException(TokenPolicy.TOKEN_EXPIRED)
                is SignatureException ->
                    JwtException(TokenPolicy.TOKEN_SIGNATURE_INVALID)
                is MalformedJwtException ->
                    JwtException(TokenPolicy.TOKEN_INVALID)
                is UnsupportedJwtException ->
                    JwtException(TokenPolicy.TOKEN_UNSUPPORTED)
                is IncorrectClaimException ->
                    JwtException(TokenPolicy.TOKEN_CLAIM_INVALID)
                else -> JwtException(TokenPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR)
            }
        }
    }

}