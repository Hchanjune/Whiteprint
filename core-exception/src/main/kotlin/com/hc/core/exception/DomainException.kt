package com.hc.core.exception

abstract class DomainException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(policy, attributes, cause)