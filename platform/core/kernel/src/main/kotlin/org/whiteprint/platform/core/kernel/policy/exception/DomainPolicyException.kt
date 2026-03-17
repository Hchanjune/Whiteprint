package org.whiteprint.platform.core.kernel.policy.exception

import org.whiteprint.platform.core.kernel.policy.Policy

abstract class DomainPolicyException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): DomainException(policy, attributes, cause)