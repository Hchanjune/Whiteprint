package com.hc.user.command.domain.model

import com.hc.core.domain.entity.Identifiable
import java.time.LocalDate

data class UserProfile(
    override val id: Long,
    val username: String,
    val locale: String,
    val timeZone: String,
    val gender: String,
    val phone: String,
    val birthDate: LocalDate,
): Identifiable<Long>