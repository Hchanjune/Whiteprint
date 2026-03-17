package com.hc.infra.security.provider.policy

import java.time.Duration

interface RefreshTokenPolicy: TokenPolicy {
    val rotationInterval: Duration
    val overlapPeriod: Duration
}