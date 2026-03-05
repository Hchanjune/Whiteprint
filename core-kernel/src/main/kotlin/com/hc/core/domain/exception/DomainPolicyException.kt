package com.hc.core.domain.exception

open class DomainPolicyException(
    errorCode: String,
    message: String,
): DomainException(errorCode, message)