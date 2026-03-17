package org.whiteprint.platform.adapter.security.provider.policy

import org.whiteprint.platform.adapter.security.provider.model.AccessTokenSigningKey

interface AccessTokenSigningKeyResolver {
    fun resolve(keyId: String): org.whiteprint.platform.adapter.security.provider.model.AccessTokenSigningKey
}