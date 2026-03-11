package com.hc.core.jwt.verifier

import com.hc.core.jwt.model.AccessTokenSigningKey

interface AccessTokenSigningKeyResolver {
    fun resolve(keyId: String): AccessTokenSigningKey
}