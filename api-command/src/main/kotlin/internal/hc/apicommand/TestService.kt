package internal.hc.apicommand

import io.github.hchanjune.operationresult.core.Operations
import io.github.hchanjune.operationresult.core.annotations.OperationManaged
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom

@Service
@OperationManaged
class TestService {

    fun test() = Operations {
        if (ThreadLocalRandom.current().nextInt(10) == 0) { // 10% 확률
            throw IllegalStateException("random failure")
        }
        "OK"
    }

    fun test2() = Operations {
        if (ThreadLocalRandom.current().nextInt(10) == 0) { // 10% 확률
            throw IllegalStateException("random failure")
        }
        "OK2"
    }


}