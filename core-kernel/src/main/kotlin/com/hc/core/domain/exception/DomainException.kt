package com.hc.core.domain.exception

abstract class DomainException(
    val errorCode: String,
    override val message: String,
    val payload: Map<String, Any> = emptyMap()
): RuntimeException(message)