package com.hc.core.exception

abstract class DomainException(
    errorCode: ErrorCode,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(errorCode, attributes, cause)