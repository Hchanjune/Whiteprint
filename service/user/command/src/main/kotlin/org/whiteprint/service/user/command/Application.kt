package org.whiteprint.service.user.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
	exclude = [
		DataSourceAutoConfiguration::class,
		HibernateJpaAutoConfiguration::class,
		DataSourceTransactionManagerAutoConfiguration::class
	]
)
//@EnableJpaRepositories(basePackages = ["com.hc.user.command.adapter.out.persistence.repository"])
class UserCommandApplication

fun main(args: Array<String>) {
	runApplication<UserCommandApplication>(*args)
}
