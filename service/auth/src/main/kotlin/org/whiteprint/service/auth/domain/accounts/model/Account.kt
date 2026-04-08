package org.whiteprint.service.auth.domain.accounts.model

import org.whiteprint.platform.core.domain.model.contract.Auditable
import org.whiteprint.platform.core.domain.model.contract.Identifiable
import org.whiteprint.service.auth.domain.accounts.vo.Email
import org.whiteprint.service.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.service.auth.domain.accounts.vo.Username
import java.time.Instant

data class Account(
    override val id: Long,
    val username: Username,
    val email: Email,
    val phoneNumber: PhoneNumber,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
): Identifiable<Long>, Auditable