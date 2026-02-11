package internal.hc.apicommand

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("test3/{id}")
    fun test3(@PathVariable id: Int) {
        val result = service.test()
        println(id)
    }

}