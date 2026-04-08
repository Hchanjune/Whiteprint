package org.whiteprint.service.auth.adapter.`in`.web.mapper

import org.whiteprint.service.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.auth.application.port.`in`.SignupCommand

fun SignupRequest.toCommand() = SignupCommand(
    username = this.username,
    email = this.email,
    phoneNumber = this.phoneNumber,
    rawPassword = this.password,
    rawPasswordCheck = this.passwordCheck
)