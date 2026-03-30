package org.whiteprint.service.user.command

import org.whiteprint.service.user.command.adapter.out.persistence.jpa.repository.UserJpaRepository
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {
    @Bean
    fun userJpaRepository(): UserJpaRepository {
        return Mockito.mock(UserJpaRepository::class.java)
    }
}