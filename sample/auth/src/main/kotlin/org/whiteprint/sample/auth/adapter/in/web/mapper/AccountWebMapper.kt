package org.whiteprint.sample.auth.adapter.`in`.web.mapper

import org.whiteprint.platform.core.kernel.clientContext.ClientContext
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.sample.auth.adapter.`in`.web.request.LoginRequest
import org.whiteprint.sample.auth.adapter.`in`.web.request.LogoutRequest
import org.whiteprint.sample.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.sample.auth.adapter.`in`.web.response.LoginResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.LogoutResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.RefreshResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.SignupResponse
import org.whiteprint.sample.auth.application.port.`in`.login.LoginCommand
import org.whiteprint.sample.auth.application.port.`in`.login.LoginResult
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutCommand
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutResult
import org.whiteprint.sample.auth.application.port.`in`.refresh.RefreshResult
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupCommand
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupResult

fun LoginRequest.toCommand(clientContext: ClientContext) = LoginCommand(
    identifier = this.identifier,
    rawPassword = this.password,
    clientContext = clientContext,
)

fun LoginResult.toResponse() = LoginResponse(
    accessToken = this.accessToken.value,
    refreshToken = this.refreshToken.value,
    failedAttempts = this.failedAttempts
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

fun RefreshResult.toResponse() = RefreshResponse(
    accessToken = this.accessToken.value,
    refreshToken = this.refreshToken.value,
)

fun LogoutRequest.toCommand(
    claims: AccessTokenClaims,
    cookieRefreshToken: String?,
): LogoutCommand = when (logoutScope) {
    LogoutRequest.Scope.CURRENT_DEVICE -> {
        val token = cookieRefreshToken
            ?: refreshToken
            ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)
        LogoutCommand.CurrentDevice(
            accessTokenId = claims.tokenId,
            accessTokenExpiresAt = claims.expiresAt,
            subject = claims.subject,
            refreshToken = RefreshToken(token),
        )
    }
    LogoutRequest.Scope.ALL_DEVICES -> {
        LogoutCommand.AllDevices(
            accessTokenId = claims.tokenId,
            accessTokenExpiresAt = claims.expiresAt,
            subject = claims.subject,
        )
    }
}

fun LogoutResult.toResponse() = LogoutResponse(
    result = this.result
)