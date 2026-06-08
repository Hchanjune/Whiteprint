package org.whiteprint.sample.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.whiteprint.platform.infra.persistence.jpa.repository.OptimizedJpaRepository

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(repositoryBaseClass = OptimizedJpaRepository::class)
class AuthApplication

fun main(args: Array<String>) {
    runApplication<org.whiteprint.sample.auth.AuthApplication>(*args)
}