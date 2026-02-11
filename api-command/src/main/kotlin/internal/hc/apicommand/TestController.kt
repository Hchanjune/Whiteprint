package internal.hc.apicommand

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val service: TestService
) {

    @GetMapping("test")
    fun test() {
        val result = service.test()
        println(result)
    }

    @GetMapping("test2")
    fun test2() {
        val result = service.test2()
        println(result)
    }

}