package org.whiteprint.sample.user.command.domain.user.model

import org.whiteprint.platform.core.domain.model.contract.Auditable
import org.whiteprint.platform.core.domain.model.contract.Identifiable
import java.time.Instant

data class User (
    override val id: Long,
    val accountId: Long,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
    override val version: Long,
): Identifiable<Long>, Auditable