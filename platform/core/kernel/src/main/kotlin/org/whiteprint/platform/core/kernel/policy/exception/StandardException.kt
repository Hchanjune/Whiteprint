package org.whiteprint.platform.core.kernel.policy.exception

import org.whiteprint.platform.core.kernel.policy.Policy

abstract class StandardException(
    val policy: Policy,
    val attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): RuntimeException(policy.message, cause) {

    val status: Int = policy.status
    val code: String = policy.code

}