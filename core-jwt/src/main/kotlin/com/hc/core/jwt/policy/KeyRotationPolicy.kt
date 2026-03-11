package com.hc.core.jwt.policy

import java.time.Duration

interface KeyRotationPolicy {
    val rotationInterval: Duration
    val overlapPeriod: Duration
}