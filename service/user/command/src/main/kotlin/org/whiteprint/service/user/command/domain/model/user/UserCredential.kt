package org.whiteprint.service.user.command.domain.model.user

import org.whiteprint.platform.core.persistence.model.contract.Identifiable

data class UserCredential(
    override val id: Long,
    val passwordHash: String,
): Identifiable<Long>