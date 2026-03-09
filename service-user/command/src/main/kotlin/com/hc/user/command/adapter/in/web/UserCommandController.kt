package com.hc.user.command.adapter.`in`.web

import com.hc.user.command.adapter.`in`.web.mapper.toCommand
import com.hc.user.command.adapter.`in`.web.mapper.toResponse
import com.hc.user.command.adapter.`in`.web.request.SignupRequest
import com.hc.user.command.adapter.`in`.web.response.UserResponse
import com.hc.user.command.application.port.`in`.SignupUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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