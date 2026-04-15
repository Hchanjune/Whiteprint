package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.policy.RevocationReason
import java.time.Duration

interface TokenRevoker {
    fun revokeToken(tokenId: String, reason: RevocationReason, duration: Duration)
    fun revokeAccount(subject: String, reason: RevocationReason, duration: Duration)

}