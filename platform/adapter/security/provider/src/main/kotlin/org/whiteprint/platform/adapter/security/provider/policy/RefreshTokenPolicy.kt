package org.whiteprint.platform.adapter.security.provider.policy

import java.time.Duration

interface RefreshTokenPolicy: org.whiteprint.platform.adapter.security.provider.policy.TokenPolicy {
    val rotationInterval: Duration
    val overlapPeriod: Duration
}