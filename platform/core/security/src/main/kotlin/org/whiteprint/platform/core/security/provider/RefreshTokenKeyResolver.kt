package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.RefreshTokenVerificationKey

@Deprecated("RefreshToken also uses asymmetric key")
interface RefreshTokenKeyResolver {
    fun resolve(): RefreshTokenVerificationKey
}