package org.whiteprint.service.user.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserCommandApplication

fun main(args: Array<String>) {
	runApplication<UserCommandApplication>(*args)
}
