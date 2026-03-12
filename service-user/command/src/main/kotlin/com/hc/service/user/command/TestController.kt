package com.hc.service.user.command

import com.hc.core.api.ApiResponse
import com.hc.infra.web.servlet.omk.ResponseEntityGenerator
import io.github.hchanjune.omk.core.annotations.ManagedController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ManagedController
class TestController(
    private val testService: TestService
) {
    @GetMapping("/test")
    fun test(): ResponseEntity<ApiResponse<String>> {
        val op = testService.operations()
        println(op.context.traceId)
        println(op.context.operation)
        println(op.context.message)
        println(op.context.method)
        println(op.context.uri)
        println(op.context.useCase)
        println(op.context.operation)
        println(op.context.issuer)
        println(op.context.type)
        println(op.context.protocol)

        return ResponseEntityGenerator.generateFromOperation(op)
    }


}