package com.hc.user.command

import io.github.hchanjune.operationresult.core.Operations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val testService: TestService
) {
    @GetMapping("/test")
    fun test(): String {
        val op = testService.operations()
        return "Hello World"
    }


}