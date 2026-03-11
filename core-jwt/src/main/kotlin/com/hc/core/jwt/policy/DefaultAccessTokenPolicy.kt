package com.hc.core.jwt.policy

import java.time.Duration

class DefaultAccessTokenPolicy(
    override val issuer: String = "Template",
    override val audience: Set<String> = setOf(
        "auth",
        "user"
    ),
    override val ttl: Duration = Duration.ofMinutes(15),
) : AccessTokenPolicy