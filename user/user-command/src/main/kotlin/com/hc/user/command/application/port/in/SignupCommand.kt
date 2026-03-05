package com.hc.user.command.application.port.`in`

import java.time.LocalDate

sealed class SignupCommand {

    data class General(
        val email: String,
        val password: String,
        val username: String,
        val locale: String?,
        val timeZone: String?,
        val gender: String?,
        val phone: String?,
        val birthDate: LocalDate?,
    ): SignupCommand()

    data class Oauth(
        val email: String,
        val provider: String,
        val providerSubject: String,
        val oauthEmail: String?,

        val username: String,
        val locale: String?,
        val timeZone: String?,
        val gender: String?,
        val phone: String?,
        val birthDate: LocalDate?,
    ): SignupCommand()

}