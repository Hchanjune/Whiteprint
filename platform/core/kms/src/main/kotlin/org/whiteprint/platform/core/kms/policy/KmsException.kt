package org.whiteprint.platform.core.kms.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class KmsException(
    policy: KmsPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): StandardException(policy, attributes, cause)