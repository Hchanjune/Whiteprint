package com.hc.user.command.adapter.`in`.web.request

import java.time.LocalDate

sealed class SignupRequest {

    data class General(
        val email: String,

        val password: String,
        val passwordCheck: String,

        val username: String,
        val locale: String?,
        val timeZone: String?,
        val gender: String?,
        val phone: String?,
        val birthDate: LocalDate?,
    ): SignupRequest()

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
    ): SignupRequest()

}