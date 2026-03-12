package com.hc.core.exception

abstract class StandardException(
    val errorCode: ErrorCode,
    val attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): RuntimeException(errorCode.message, cause) {

    val status: Int = errorCode.status
    val code: String = errorCode.code

}