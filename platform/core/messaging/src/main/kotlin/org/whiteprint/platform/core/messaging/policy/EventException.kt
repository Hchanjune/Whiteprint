package org.whiteprint.platform.core.messaging.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class EventException(
    policy: org.whiteprint.platform.core.messaging.policy.EventPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)