package org.whiteprint.platform.core.kernel.policy.exception

import org.whiteprint.platform.core.kernel.policy.Policy

abstract class DomainValidationException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
): DomainException(policy, attributes)