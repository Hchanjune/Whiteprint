package org.whiteprint.sample.user.command.adapter.`in`.web.response

import org.whiteprint.platform.core.domain.model.contract.Auditable
import org.whiteprint.platform.core.domain.model.contract.Identifiable
import java.time.Instant
import java.time.LocalDate

data class UserResponse(
    override val id: String,
    val email: String,
    val lastLogin: Instant?,
    val isAccountLocked: Boolean,
    val isAccountAvailable: Boolean,

    val username: String,
    val locale: String?,
    val timeZone: String?,
    val gender: String?,
    val phone: String?,
    val birthDate: LocalDate?,
    override val insertedAt: Instant,
    override val updatedAt: Instant,
    override val isDeleted: Boolean,
    override val deletedAt: Instant?,
    override val version: Long,
): Identifiable<String>, Auditable