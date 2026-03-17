package com.hc.infra.security.verifier.policy

import com.hc.infra.security.verifier.model.AccessTokenVerificationKey

interface AccessTokenVerificationKeyResolver {
    fun resolve(keyId: String): AccessTokenVerificationKey
}