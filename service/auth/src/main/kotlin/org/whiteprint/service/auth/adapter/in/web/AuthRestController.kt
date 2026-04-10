package org.whiteprint.service.auth.adapter.`in`.web

import io.github.hchanjune.omk.core.annotations.ManagedController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.whiteprint.platform.adapter.web.servlet.omk.ResponseEntityGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.service.auth.adapter.`in`.web.mapper.toCommand
import org.whiteprint.service.auth.adapter.`in`.web.mapper.toResponse
import org.whiteprint.service.auth.adapter.`in`.web.request.LoginRequest
import org.whiteprint.service.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.auth.adapter.`in`.web.response.LoginResponse
import org.whiteprint.service.auth.adapter.`in`.web.response.SignupResponse
import org.whiteprint.service.auth.application.port.`in`.LoginUseCase
import org.whiteprint.service.auth.application.port.`in`.SignupUseCase
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicyException

@ManagedController
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController(
    private val signupUserCase: SignupUseCase,
    private val loginUseCase: LoginUseCase
) {

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val command = loginRequest.toCommand()
        val operation = loginUseCase.handle(command)
        if (operation.data.accessToken == null || operation.data.refreshToken == null) {
            throw AccountPolicyException(AccountPolicy.LOGIN_FAILURE)
        }
        return ResponseEntityGenerator.generateFromOperation(operation) {
            operation.data.toResponse()
        }
    }

    @PostMapping("/logout")
    fun logout() {

    }

    @PostMapping("/signup")
    fun signup(
        @RequestBody signupRequest: SignupRequest
    ): ResponseEntity<ApiResponse<SignupResponse>> {
        val command = signupRequest.toCommand()
        val operation = signupUserCase.handle(command)
        return ResponseEntityGenerator.generateFromOperation(operation) {
            operation.data.toResponse()
        }
    }

    @PostMapping("/refresh")
    fun refresh() {

    }

}