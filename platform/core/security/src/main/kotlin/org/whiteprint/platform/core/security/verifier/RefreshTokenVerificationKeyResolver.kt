package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.model.RefreshTokenVerificationKey

interface RefreshTokenVerificationKeyResolver {
    fun resolve(keyAlias: String, keyVersion: String?): RefreshTokenVerificationKey
}