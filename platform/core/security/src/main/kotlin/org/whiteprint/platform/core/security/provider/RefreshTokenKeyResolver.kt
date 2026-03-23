package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.RefreshTokenKey

interface RefreshTokenKeyResolver {
    fun resolve(): RefreshTokenKey
}