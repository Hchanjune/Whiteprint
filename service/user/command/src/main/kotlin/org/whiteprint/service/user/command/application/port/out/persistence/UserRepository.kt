package org.whiteprint.service.user.command.application.port.out.persistence

import org.whiteprint.platform.core.domain.repository.AggregateRepository
import org.whiteprint.service.user.command.domain.user.aggregate.UserAggregate

interface UserRepository: AggregateRepository<Long, UserAggregate> {

    fun findByEmailOrThrow(email: String): UserAggregate

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

}