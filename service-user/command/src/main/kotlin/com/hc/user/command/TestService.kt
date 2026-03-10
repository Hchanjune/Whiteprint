package com.hc.user.command

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.OperationManaged
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service

@Service
@OperationManaged
class TestService {

    @OperationManaged(operation = "operation", useCase = "test")
    fun operations(): OperationResult<String> = Operations {
        "hello"
    }

}