package org.whiteprint.service.user.command.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.whiteprint.service.user.command.adapter.`in`.web.mapper.toCommand
import org.whiteprint.service.user.command.adapter.`in`.web.mapper.toResponse
import org.whiteprint.service.user.command.adapter.`in`.web.request.SignupRequest
import org.whiteprint.service.user.command.adapter.`in`.web.response.UserResponse
import org.whiteprint.service.user.command.application.port.`in`.SignupUseCase

@RestController
@RequestMapping("/api/v1/users")
class UserCommandController(
    private val signupUseCase: SignupUseCase
) {

    @PostMapping("/signup")
    fun signUp(
        @RequestBody request: SignupRequest.General
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(signupUseCase.handle(request.toCommand()).data.toResponse())
    }

    @PostMapping("/signup/oauth")
    fun signUp(
        @RequestBody request: SignupRequest.Oauth
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(signupUseCase.handle(request.toCommand()).data.toResponse())
    }

}