package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.model.AccessTokenVerificationKey

interface AccessTokenVerificationKeyResolver {
    fun resolve(keyId: String): AccessTokenVerificationKey
}