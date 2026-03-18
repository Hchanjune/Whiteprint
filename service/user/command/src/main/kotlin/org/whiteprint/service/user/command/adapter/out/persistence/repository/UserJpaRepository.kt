package org.whiteprint.service.user.command.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.whiteprint.service.user.command.adapter.out.persistence.entity.UserEntity

interface UserJpaRepository: JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByProfile_Username(username: String): Boolean
}