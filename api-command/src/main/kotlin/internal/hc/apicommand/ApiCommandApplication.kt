package internal.hc.apicommand

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiCommandApplication

fun main(args: Array<String>) {
	runApplication<ApiCommandApplication>(*args)
}
