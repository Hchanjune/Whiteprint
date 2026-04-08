package org.whiteprint.service.auth.adapter.`in`.web

import io.github.hchanjune.omk.core.annotations.ManagedController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagedController
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController(

) {

    @PostMapping("/login")
    fun login() {

    }

    @PostMapping("/logout")
    fun logout() {

    }

    @PostMapping("/signup")
    fun signup() {

    }

    @PostMapping("/refresh")
    fun refresh() {

    }

}