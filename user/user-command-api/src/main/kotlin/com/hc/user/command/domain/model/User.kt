package com.hc.user.command.domain.model

import com.hc.core.domain.entity.Auditable
import com.hc.core.domain.entity.Identifiable
import com.hc.core.domain.entity.Versioned
import java.time.Instant

data class User (
    override val id: Long,
    val email: String,
    val lastLogin: Instant,
    val isAccountLocked: Boolean,
    val isAccountAvailable: Boolean,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
    override val version: Long,
): Identifiable<Long>, Auditable, Versioned