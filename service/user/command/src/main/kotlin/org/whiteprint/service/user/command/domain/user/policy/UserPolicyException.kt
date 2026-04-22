package org.whiteprint.service.user.command.domain.user.policy

import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException

class UserPolicyException(
    policy: UserPolicy,
    attributes: Map<String, Any> = emptyMap(),
): DomainPolicyException(policy, attributes)