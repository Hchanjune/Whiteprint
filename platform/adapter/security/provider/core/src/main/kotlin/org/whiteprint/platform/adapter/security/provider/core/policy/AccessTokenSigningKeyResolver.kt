package org.whiteprint.platform.adapter.security.provider.core.policy

import org.whiteprint.platform.adapter.security.provider.core.model.AccessTokenSigningKey

interface AccessTokenSigningKeyResolver {
    fun resolve(keyId: String): AccessTokenSigningKey
}