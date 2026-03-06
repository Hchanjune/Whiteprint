package com.hc.user.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
	exclude = [
		DataSourceAutoConfiguration::class,
		HibernateJpaAutoConfiguration::class,
		DataSourceTransactionManagerAutoConfiguration::class
	]
)
//@EnableJpaRepositories(basePackages = ["com.hc.user.command.adapter.out.persistence.repository"])
class ApiCommandApplication

fun main(args: Array<String>) {
	runApplication<ApiCommandApplication>(*args)
}
