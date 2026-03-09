package com.hc.user.command

import com.hc.core.api.ApiResponse
import com.hc.web.servlet.omk.ResponseEntityGenerator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val testService: TestService
) {
    @GetMapping("/test")
    fun test(): ResponseEntity<ApiResponse<String>> {
        val op = testService.operations()
        println(op.operation)
        println(op.metrics)
        println(op.telemetry)
        return ResponseEntityGenerator.generateFromOperation(op)
    }


}