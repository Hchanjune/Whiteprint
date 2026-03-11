package com.hc.auth.adapter.`in`.web

import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagedResource
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController {

    @PostMapping("/login")
    fun login() {

    }

    @PostMapping("/logout")
    fun logout() {

    }

    @PostMapping("/refresh")
    fun refresh() {

    }

}