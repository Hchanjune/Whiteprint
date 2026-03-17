package org.whiteprint.platform.infra.persistence.jpa.policy

import org.whiteprint.platform.core.kernel.policy.Policy
import org.whiteprint.platform.core.kernel.policy.exception.StandardException

open class EntityException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)