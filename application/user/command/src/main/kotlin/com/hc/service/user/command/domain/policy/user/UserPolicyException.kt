package com.hc.service.user.command.domain.policy.user

import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException

class UserPolicyException(
    errorCode: UserPolicy,
    attributes: Map<String, Any> = emptyMap(),
): DomainPolicyException(errorCode, attributes)