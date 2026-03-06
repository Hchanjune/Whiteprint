package com.hc.user.command.application.port.out

import com.hc.core.domain.repository.AggregateRepository
import com.hc.user.command.domain.model.user.UserAggregate


interface UserRepository: AggregateRepository<Long, UserAggregate> {

    fun findByEmailOrThrow(email: String): UserAggregate

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean


}