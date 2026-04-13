package org.whiteprint.platform.core.security.model

interface TokenPayload {
    val jti: String
    val sub: String
    val iss: String
    val aud: Set<String>
    val iat: Long
    val exp: Long
}