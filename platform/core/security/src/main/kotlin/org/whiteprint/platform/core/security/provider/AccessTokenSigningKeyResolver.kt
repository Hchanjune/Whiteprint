package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessTokenSigningKey

interface AccessTokenSigningKeyResolver {
    fun resolve(keyId: String): AccessTokenSigningKey
}