package org.whiteprint.service.auth.adapter.`in`.web.mapper

import org.whiteprint.service.auth.adapter.`in`.web.request.LoginRequest
import org.whiteprint.service.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.auth.adapter.`in`.web.response.LoginResponse
import org.whiteprint.service.auth.adapter.`in`.web.response.SignupResponse
import org.whiteprint.service.auth.application.port.`in`.LoginCommand
import org.whiteprint.service.auth.application.port.`in`.LoginResult
import org.whiteprint.service.auth.application.port.`in`.SignupCommand
import org.whiteprint.service.auth.application.port.`in`.SignupResult

fun LoginRequest.toCommand() = LoginCommand(
    identifier = this.identifier,
    rawPassword = this.password
)

fun LoginResult.toResponse() = LoginResponse(
    accessToken = this.accessToken?.value?: "",
    refreshToken = this.refreshToken?.value?: ""
)

fun SignupRequest.toCommand() = SignupCommand(
    username = this.username,
    email = this.email,
    phoneNumber = this.phoneNumber,
    rawPassword = this.password,
    rawPasswordCheck = this.passwordCheck
)

fun SignupResult.toResponse() = SignupResponse(
    id = this.id.toString(),
    username = this.username.value,
    email = this.email.value,
    phoneNumber = this.phoneNumber.value,
    signedUpAt = this.signedUpAt
)