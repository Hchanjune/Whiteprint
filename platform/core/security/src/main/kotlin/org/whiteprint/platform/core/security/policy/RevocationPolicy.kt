package org.whiteprint.platform.core.security.policy

import java.time.Duration

data class RevocationPolicy(
    val accountRevocationDuration: Duration
)