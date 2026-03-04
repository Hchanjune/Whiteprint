package com.hc.user.command.adapter.`in`.web.response

import java.time.Instant
import java.time.LocalDate

data class UserResponse(
    val id: String,
    val email: String,
    val lastLogin: Instant?,
    val isAccountLocked: Boolean,
    val isAccountAvailable: Boolean,

    val username: String,
    val locale: String,
    val timeZone: String,
    val gender: String,
    val phone: String,
    val birthDate: LocalDate,
    val insertedAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val version: Long,
)