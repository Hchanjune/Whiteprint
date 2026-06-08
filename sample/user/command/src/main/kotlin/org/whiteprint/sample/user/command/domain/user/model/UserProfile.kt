package org.whiteprint.sample.user.command.domain.user.model

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