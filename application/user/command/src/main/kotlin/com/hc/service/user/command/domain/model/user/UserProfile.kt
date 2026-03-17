package com.hc.service.user.command.domain.model.user

import org.whiteprint.platform.core.domain.model.contract.Identifiable
import java.time.LocalDate

data class UserProfile(
    override val id: Long,
    val username: String,
    val locale: String?,
    val timeZone: String?,
    val gender: String?,
    val phone: String?,
    val birthDate: LocalDate?,
): Identifiable<Long>