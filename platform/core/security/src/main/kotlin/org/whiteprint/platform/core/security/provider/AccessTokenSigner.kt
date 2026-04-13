package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.AccessTokenSigningResult

interface AccessTokenSigner {
    fun getLatestSigningKeyMetadata(): AccessTokenSigningKeyMetadata
    fun sign(rawText: String): AccessTokenSigningResult
}