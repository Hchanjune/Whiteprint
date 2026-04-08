package org.whiteprint.service.auth.domain.accounts.model

import org.whiteprint.platform.core.domain.model.contract.Identifiable
import org.whiteprint.service.auth.domain.accounts.vo.PasswordHash

data class Credential(
    override val id: Long,
    val accountId: Long,
    val passwordHash: PasswordHash,
): Identifiable<Long>