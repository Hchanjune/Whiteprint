package com.hc.core.exception

abstract class DomainPolicyException(
    errorCode: ErrorCode,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): DomainException(errorCode, attributes, cause)