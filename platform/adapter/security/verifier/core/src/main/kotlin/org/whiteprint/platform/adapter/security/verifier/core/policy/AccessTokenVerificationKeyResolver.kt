package org.whiteprint.platform.adapter.security.verifier.core.policy

import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenVerificationKey

interface AccessTokenVerificationKeyResolver {
    fun resolve(keyId: String): AccessTokenVerificationKey
}