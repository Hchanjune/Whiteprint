package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.RefreshTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.RefreshTokenSigningResult

interface RefreshTokenSigner {
    fun getLatestSigningKeyMetadata(): RefreshTokenSigningKeyMetadata
    /** [signingKey] 의 버전으로 서명한다 — 헤더에 적은 버전과 서명 버전이 같아야 검증된다(사이에 키가 회전해도). */
    fun sign(signingKey: RefreshTokenSigningKeyMetadata, text: String): RefreshTokenSigningResult
}