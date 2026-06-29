package org.whiteprint.platform.core.lock.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class LockException(
    policy: LockPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)
