package internal.hc.api.query

import kotlinx.coroutines.delay
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    suspend fun test(): ResponseEntity<String> {
        MDC.put("cid", "CID-${System.currentTimeMillis()}")

        log("start")
        logMdc("start")

        delay(200) // suspend

        log("after delay")
        logMdc("after delay")

        MDC.clear()
        return ResponseEntity.ok("test")
    }



    private fun log(tag: String) {
        println("[$tag] thread=${Thread.currentThread().name}")
    }

    private fun logMdc(tag: String) {
        println("[$tag] MDC.cid=${MDC.get("cid")}")
    }
}
