package com.hc.core.jwt.policy

import java.time.Duration

interface TokenPolicy {
    val issuer: String
    val audience: Set<String>
    val ttl: Duration
}