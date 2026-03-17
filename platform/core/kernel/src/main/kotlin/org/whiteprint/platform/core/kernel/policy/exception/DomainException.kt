package org.whiteprint.platform.core.kernel.policy.exception

import org.whiteprint.platform.core.kernel.policy.Policy

abstract class DomainException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(policy, attributes, cause)