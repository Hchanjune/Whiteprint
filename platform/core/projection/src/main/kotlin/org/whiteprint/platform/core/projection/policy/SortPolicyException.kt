package org.whiteprint.platform.core.projection.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class SortPolicyException(
    policy: SortPolicy,
    attributes: Map<String, Any> = emptyMap(),
    throwable: Throwable? = null
): StandardException(policy, attributes, throwable)