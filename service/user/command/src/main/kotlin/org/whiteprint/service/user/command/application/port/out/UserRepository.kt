package org.whiteprint.service.user.command.application.port.out

import org.whiteprint.platform.core.domain.repository.AggregateRepository
import org.whiteprint.service.user.command.domain.model.user.UserAggregate

interface UserRepository: AggregateRepository<Long, UserAggregate> {

    fun findByEmailOrThrow(email: String): UserAggregate

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

}