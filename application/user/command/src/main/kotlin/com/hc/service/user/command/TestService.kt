package com.hc.service.user.command

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service

@Service
@ManagedService
class TestService {

    @ManagedOperation(operation = "operation", useCase = "test")
    fun operations(): OperationResult<String> = Operations {
        throw IllegalArgumentException("error")
        "hello"
    }

}