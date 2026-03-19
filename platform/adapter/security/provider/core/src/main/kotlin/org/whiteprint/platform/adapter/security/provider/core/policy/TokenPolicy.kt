package org.whiteprint.platform.adapter.security.provider.core.policy

import java.time.Duration

interface TokenPolicy {
    val issuer: String
    val audience: Set<String>
    val ttl: Duration
}