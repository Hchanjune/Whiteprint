package com.hc.core.jwt.verifier

import com.hc.core.jwt.model.AccessTokenVerificationKey

interface AccessTokenVerificationKeyResolver {
    fun resolve(keyId: String): AccessTokenVerificationKey
}