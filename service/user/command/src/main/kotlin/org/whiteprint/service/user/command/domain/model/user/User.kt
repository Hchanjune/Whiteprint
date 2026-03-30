package org.whiteprint.service.user.command.domain.model.user

import org.whiteprint.platform.core.persistence.model.contract.Auditable
import org.whiteprint.platform.core.persistence.model.contract.Identifiable
import java.time.Instant

data class User (
    override val id: Long,
    val email: String,
    val lastLogin: Instant?,
    val isAccountLocked: Boolean,
    val isAccountAvailable: Boolean,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
    override val version: Long,
): Identifiable<Long>, Auditable