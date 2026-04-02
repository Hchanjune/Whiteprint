package org.whiteprint.service.user.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class UserCommandApplication

fun main(args: Array<String>) {
	runApplication<UserCommandApplication>(*args)
}
