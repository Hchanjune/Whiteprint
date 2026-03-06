package com.hc.user.command

import com.hc.user.command.adapter.out.persistence.repository.UserJpaRepository
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