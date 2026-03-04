package com.hc.user.command.domain.model

import com.hc.core.domain.contract.Identifiable

data class UserCredential(
    override val id: Long,
    val passwordHash: String,
): Identifiable<Long>