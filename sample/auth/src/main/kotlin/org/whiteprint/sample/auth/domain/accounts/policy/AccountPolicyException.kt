package org.whiteprint.sample.auth.domain.accounts.policy

import org.whiteprint.platform.core.kernel.policy.exception.DomainPolicyException

class AccountPolicyException(
    policy: AccountPolicy,
    attributes: Map<String, Any> = emptyMap(),
): DomainPolicyException(policy, attributes)