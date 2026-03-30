package org.whiteprint.service.user.command.domain.model.user

import org.whiteprint.platform.core.persistence.model.contract.Auditable
import org.whiteprint.platform.core.persistence.model.contract.Identifiable
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