package org.whiteprint.platform.adapter.security.provider.core.policy

import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenKey

interface RefreshTokenKeyResolver {
    fun resolve(keyId: String): RefreshTokenKey
}