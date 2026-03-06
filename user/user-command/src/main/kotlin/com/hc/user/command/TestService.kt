package com.hc.user.command

import io.github.hchanjune.operationresult.core.Operations
import io.github.hchanjune.operationresult.core.annotations.OperationManaged
import io.github.hchanjune.operationresult.core.models.OperationResult
import org.springframework.stereotype.Service

@Service
@OperationManaged
class TestService {

    @OperationManaged
    fun operations(): OperationResult<String> = Operations {
        println(Operations.current.toString())
        println(Operations.metrics.toString())
        println(Operations.telemetry.toString())
        "hello"
    }

}