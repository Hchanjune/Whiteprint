package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessTokenSigningKeyMetadata
import org.whiteprint.platform.core.security.model.AccessTokenSigningResult

interface AccessTokenSigner {
    fun getLatestSigningKeyMetadata(): AccessTokenSigningKeyMetadata
    /** [signingKey] 의 버전으로 서명한다 — 헤더에 적은 버전과 서명 버전이 같아야 검증된다(사이에 키가 회전해도). */
    fun sign(signingKey: AccessTokenSigningKeyMetadata, rawText: String): AccessTokenSigningResult
}