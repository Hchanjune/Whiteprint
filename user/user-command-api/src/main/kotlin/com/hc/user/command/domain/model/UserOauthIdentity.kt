package com.hc.user.command.domain.model

import com.hc.core.domain.contract.Auditable
import com.hc.core.domain.contract.Identifiable
import java.time.Instant

data class UserOauthIdentity(
    override val id: Long,
    val provider: String,
    val providerSubject: String,
    val email: String?,
    val isEmailVerified: Boolean,
    val linkedAt: Instant,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
): Identifiable<Long>, Auditable