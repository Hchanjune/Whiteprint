package org.whiteprint.sample.auth.domain.accounts.policy

import org.whiteprint.platform.core.kernel.policy.exception.DomainValidationException

class AccountValidationException(
    policy: AccountPolicy,
    attributes: Map<String, Any> = emptyMap(),
): DomainValidationException(policy, attributes)