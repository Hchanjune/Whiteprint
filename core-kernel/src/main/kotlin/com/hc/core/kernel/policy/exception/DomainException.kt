package com.hc.core.kernel.policy.exception

import com.hc.core.kernel.policy.Policy

abstract class DomainException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(policy, attributes, cause)