package org.whiteprint.platform.core.domain.policy

import org.whiteprint.platform.core.kernel.policy.Policy
import org.whiteprint.platform.core.kernel.policy.exception.StandardException

open class PersistenceException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)