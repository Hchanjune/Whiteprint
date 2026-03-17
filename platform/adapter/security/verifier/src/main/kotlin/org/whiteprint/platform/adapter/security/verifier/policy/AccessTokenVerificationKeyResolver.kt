package org.whiteprint.platform.adapter.security.verifier.policy

import org.whiteprint.platform.adapter.security.verifier.model.AccessTokenVerificationKey

interface AccessTokenVerificationKeyResolver {
    fun resolve(keyId: String): AccessTokenVerificationKey
}