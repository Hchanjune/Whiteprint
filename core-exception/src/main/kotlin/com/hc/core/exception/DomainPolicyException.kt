package com.hc.core.exception

abstract class DomainPolicyException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): DomainException(policy, attributes, cause)