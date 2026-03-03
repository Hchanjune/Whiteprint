package com.hc.user.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiCommandApplication

fun main(args: Array<String>) {
	runApplication<ApiCommandApplication>(*args)
}
