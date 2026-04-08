package org.whiteprint.service.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.infra.persistence.jpa.repository.OptimizedJpaRepository

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = OptimizedJpaRepository::class)
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}