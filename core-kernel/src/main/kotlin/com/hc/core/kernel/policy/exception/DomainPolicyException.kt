package com.hc.core.kernel.policy.exception

import com.hc.core.kernel.policy.Policy

abstract class DomainPolicyException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): DomainException(policy, attributes, cause)