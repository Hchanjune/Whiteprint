package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.RefreshTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.RefreshTokenSigningResult

interface RefreshTokenSigner {
    fun getLatestSigningKeyMetadata(): RefreshTokenSigningKeyMetadata
    fun sign(text: String): RefreshTokenSigningResult
}