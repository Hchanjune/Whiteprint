package com.hc.service.user.command.domain.model.user

import com.hc.core.domain.contract.Identifiable

data class UserCredential(
    override val id: Long,
    val passwordHash: String,
): Identifiable<Long>