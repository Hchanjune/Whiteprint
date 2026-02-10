package internal.hc.apicommand

import io.github.hchanjune.operationresult.core.Operations
import io.github.hchanjune.operationresult.core.annotations.OperationManaged
import org.springframework.stereotype.Service

@Service
@OperationManaged
class TestService {


    @OperationManaged
    fun test() = Operations {
        "OK"
    }

}