package com.hc.user.command.domain.model.user

import com.hc.core.domain.contract.Auditable
import com.hc.core.domain.contract.Identifiable
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