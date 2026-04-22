package org.whiteprint.service.user.command.adapter.`in`.web.mapper

import org.whiteprint.service.user.command.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.user.command.adapter.`in`.web.response.UserResponse
import org.whiteprint.service.user.command.application.port.`in`.command.SignupCommand
import org.whiteprint.service.user.command.domain.user.aggregate.UserAggregate


fun SignupRequest.General.toCommand(): SignupCommand.General {
    require(this.password == this.passwordCheck)

    return SignupCommand.General(
        email = this.email,
        password = this.password,
        username = this.username,
        locale = this.locale,
        timeZone = this.timeZone,
        gender = this.gender,
        phone = this.phone,
        birthDate = this.birthDate,
    )
}

fun SignupRequest.Oauth.toCommand(): SignupCommand.Oauth {
    return SignupCommand.Oauth(
        email = this.email,
        provider = this.provider,
        providerSubject = this.providerSubject,
        oauthEmail = this.oauthEmail,
        username = this.username,
        locale = this.locale,
        timeZone = this.timeZone,
        gender = this.gender,
        phone = this.phone,
        birthDate = this.birthDate,
    )
}

fun UserAggregate.toResponse(): UserResponse {
    return UserResponse(
        id = this.id.toString(),
        email = this.email,
        lastLogin = this.lastLogin,
        isAccountLocked = this.isAccountLocked,
        isAccountAvailable = this.isAccountAvailable,

        username = this.username,
        locale = this.locale,
        timeZone = this.timeZone,
        gender = this.gender,
        phone = this.phone,
        birthDate = this.birthDate,
        insertedAt = this.insertedAt,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        deletedAt = this.deletedAt,
        version = this.version
    )
}