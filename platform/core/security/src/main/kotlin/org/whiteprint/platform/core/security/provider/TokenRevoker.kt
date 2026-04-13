package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.policy.RevocationReason
import java.time.Duration

interface TokenRevoker {
    fun revokeAccessToken(tokenId: String, reason: RevocationReason, duration: Duration)
    fun revokeRefreshToken(subject: String, reason: RevocationReason, duration: Duration)
}