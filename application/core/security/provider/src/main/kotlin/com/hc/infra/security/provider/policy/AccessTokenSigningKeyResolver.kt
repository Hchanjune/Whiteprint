package com.hc.infra.security.provider.policy

import com.hc.infra.security.provider.model.AccessTokenSigningKey

interface AccessTokenSigningKeyResolver {
    fun resolve(keyId: String): AccessTokenSigningKey
}