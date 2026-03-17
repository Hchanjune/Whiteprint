package com.hc.infra.security.provider.policy

import com.hc.infra.security.provider.model.RefreshTokenKey

interface RefreshTokenKeyResolver {
    fun resolve(keyId: String): RefreshTokenKey
}