package org.whiteprint.platform.core.security.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class SecurityException (
    policy: SecurityPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(policy, attributes, cause)