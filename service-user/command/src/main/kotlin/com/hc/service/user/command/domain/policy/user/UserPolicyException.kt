package com.hc.service.user.command.domain.policy.user

import com.hc.core.exception.DomainPolicyException

class UserPolicyException(
    errorCode: UserPolicies,
    attributes: Map<String, Any> = emptyMap(),
): DomainPolicyException(errorCode, attributes)