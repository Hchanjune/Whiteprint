package com.hc.core.jwt.exception

sealed class JwtException (
    val statusCode: Int,
    val errorCode: String,
    override val message: String,
): RuntimeException(message) {

    companion object {
        fun mapFrom(exception: Exception): JwtException {
            return when (exception) {
                is JwtException -> exception
                is io.jsonwebtoken.ExpiredJwtException ->
                    TokenExpiredException()
                is io.jsonwebtoken.security.SignatureException ->
                    TokenSignatureException()
                is io.jsonwebtoken.MalformedJwtException ->
                    TokenInvalidException()
                is io.jsonwebtoken.UnsupportedJwtException ->
                    TokenUnsupportedException()
                is io.jsonwebtoken.IncorrectClaimException ->
                    TokenClaimInvalidException()
                else -> InternalException()
            }
        }
    }

    class TokenInvalidException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_INVALID",
        message: String = "The provided token is invalid."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class TokenExpiredException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_EXPIRED",
        message: String = "The provided token is expired."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class TokenUnsupportedException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_UNSUPPORTED",
        message: String = "The provided token format is not supported."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class TokenNotFoundException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_NOT_FOUND",
        message: String = "Authorization token is missing."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class AccessTokenNeedsUpdateException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_NEEDS_UPDATE",
        message: String = "User information has changed. Please refresh your token."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class RefreshTokenBlacklistedException(
        statusCode: Int = 403,
        errorCode: String = "REFRESH_TOKEN_BLACKLISTED",
        message: String = "Refresh Token is blacklisted by logout. Please re-login."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class TokenClaimInvalidException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_CLAIM_INVALID",
        message: String = "The provided token contains invalid or unexpected claims."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class TokenSignatureException(
        statusCode: Int = 401,
        errorCode: String = "TOKEN_SIGNATURE_INVALID",
        message: String = "The token signature validation failed."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

    class InternalException(
        statusCode: Int = 500,
        errorCode: String = "TOKEN_VERIFICATION_INTERNAL_ERROR",
        message: String = "An unexpected error occurred during token verification."
    ): JwtException(
        statusCode = statusCode,
        errorCode = errorCode,
        message = message
    )

}