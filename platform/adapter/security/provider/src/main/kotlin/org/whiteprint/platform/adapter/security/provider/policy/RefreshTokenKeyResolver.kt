package org.whiteprint.platform.adapter.security.provider.policy

import org.whiteprint.platform.adapter.security.provider.model.RefreshTokenKey

interface RefreshTokenKeyResolver {
    fun resolve(keyId: String): org.whiteprint.platform.adapter.security.provider.model.RefreshTokenKey
}