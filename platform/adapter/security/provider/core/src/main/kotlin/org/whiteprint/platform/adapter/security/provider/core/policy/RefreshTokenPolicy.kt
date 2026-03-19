package org.whiteprint.platform.adapter.security.provider.core.policy

import java.time.Duration

interface RefreshTokenPolicy: TokenPolicy {
    val rotationInterval: Duration
    val overlapPeriod: Duration
}