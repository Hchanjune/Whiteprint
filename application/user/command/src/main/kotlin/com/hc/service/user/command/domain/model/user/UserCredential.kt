package com.hc.service.user.command.domain.model.user

import org.whiteprint.platform.core.domain.model.contract.Identifiable

data class UserCredential(
    override val id: Long,
    val passwordHash: String,
): Identifiable<Long>