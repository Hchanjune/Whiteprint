package internal.hc.apicommand

import io.github.hchanjune.operationresult.core.Operations
import io.github.hchanjune.operationresult.core.annotations.OperationManaged
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom

@Service
@OperationManaged(
    operation = "TestService",
)
class TestService {

    @OperationManaged(event = "TestEvent")
    fun test() = Operations {
        if (ThreadLocalRandom.current().nextInt(10) == 0) { // 10% 확률
            throw IllegalStateException("random failure")
        }
        "OK"
    }

    @OperationManaged(event = "TestEvent2")
    fun test2() = Operations {
        if (ThreadLocalRandom.current().nextInt(10) == 0) { // 10% 확률
            throw IllegalStateException("random failure")
        }
        "OK2"
    }


}