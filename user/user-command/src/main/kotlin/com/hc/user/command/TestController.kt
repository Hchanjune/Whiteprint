package com.hc.user.command

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val testService: TestService
) {
    @GetMapping("/test")
    fun test(): String {
        val op = testService.operations()
        println(op.operation)
        println(op.metrics)
        println(op.telemetry)
        return "Hello World"
    }


}