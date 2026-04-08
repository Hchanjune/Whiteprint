package org.whiteprint.service.auth.domain.accounts

import org.whiteprint.platform.core.domain.model.contract.Identifiable
import org.whiteprint.service.auth.domain.accounts.vo.PasswordHash

data class Credential(
    override val id: Long,
    val passwordHash: PasswordHash,
): Identifiable<Long>