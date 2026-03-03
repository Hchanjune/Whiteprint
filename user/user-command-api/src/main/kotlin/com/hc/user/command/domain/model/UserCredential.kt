package com.hc.user.command.domain.model

import com.hc.core.domain.entity.Identifiable

data class UserCredential(
    override val id: Long,
    val userId: Long,
    val passwordHash: String,
): Identifiable<Long>