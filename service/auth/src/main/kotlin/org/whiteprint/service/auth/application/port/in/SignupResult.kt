package org.whiteprint.service.auth.application.port.`in`

import org.whiteprint.service.auth.domain.accounts.vo.Email
import org.whiteprint.service.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.service.auth.domain.accounts.vo.Username
import java.time.Instant

data class SignupResult (
    val id: Long,
    val username: Username,
    val email: Email,
    val phoneNumber: PhoneNumber,
    val signedUpAt: Instant,
)