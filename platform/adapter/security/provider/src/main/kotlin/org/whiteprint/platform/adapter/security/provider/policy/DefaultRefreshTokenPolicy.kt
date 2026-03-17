package org.whiteprint.platform.adapter.security.provider.policy

import java.time.Duration

class DefaultRefreshTokenPolicy(
    override val issuer: String = "Template",
    override val audience: Set<String> = setOf(
        "auth"
    ),
    override val ttl: Duration = Duration.ofDays(30),
    override val rotationInterval: Duration,
    override val overlapPeriod: Duration
): RefreshTokenPolicy