package org.whiteprint.service.auth.adapter.`in`.web

import io.github.hchanjune.omk.core.annotations.ManagedController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.whiteprint.service.auth.adapter.`in`.web.mapper.toCommand
import org.whiteprint.service.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.auth.application.port.`in`.SignupUseCase

@ManagedController
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController(
    private val signupUserCase: SignupUseCase
) {

    @PostMapping("/login")
    fun login() {

    }

    @PostMapping("/logout")
    fun logout() {

    }

    @PostMapping("/signup")
    fun signup(
        @RequestBody signupRequest: SignupRequest
    ) {
        val command = signupRequest.toCommand()
        val response = signupUserCase.handle(command)
    }

    @PostMapping("/refresh")
    fun refresh() {

    }

}