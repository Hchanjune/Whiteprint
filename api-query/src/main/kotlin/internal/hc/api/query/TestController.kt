package internal.hc.api.query

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    suspend fun test(): ResponseEntity<String> {
        log("start")

        kotlinx.coroutines.delay(200) // suspend 지점 (non-blocking)

        log("after delay")
        return ResponseEntity.ok("test")
    }

    private fun log(tag: String) {
        println("[$tag] thread=${Thread.currentThread().name}")
    }
}
